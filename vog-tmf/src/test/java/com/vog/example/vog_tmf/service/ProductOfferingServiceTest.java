package com.vog.example.vog_tmf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vog.example.vog_tmf.dto.ProductOfferingCreate;
import com.vog.example.vog_tmf.dto.ProductOfferingTmf;
import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.entity.ProductOffering;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.exception.InvalidInputException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ProductOfferingServiceTest {

    @Mock
    ProductOfferingRepository offerings;

    @Mock
    ProductSpecificationRepository specs;

    @Mock
    CategoryRepository categories;

    @InjectMocks
    ProductOfferingService service;

    private ProductOffering saved(long id, String name, String status) {
        ProductOffering o = new ProductOffering();
        o.setId(id);
        o.setName(name);
        o.setLifecycleStatus(status);
        return o;
    }

    @Test
    void create_resolvesRefsAndBuildsRefRepresentations() {
        ProductSpecification spec = new ProductSpecification();
        spec.setId(3L);
        spec.setName("5G SIM-Only Spec");
        Category cat = new Category();
        cat.setId(2L);
        cat.setName("Mobile");
        when(specs.findById(3L)).thenReturn(Optional.of(spec));
        when(categories.findById(2L)).thenReturn(Optional.of(cat));
        when(offerings.save(any())).thenAnswer(inv -> {
            ProductOffering o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        ProductOfferingTmf out = service.create(new ProductOfferingCreate(
                "Mobile 5G Unlimited", null, false, true, "Active", null,
                new ProductOfferingCreate.RefId("3"),
                List.of(new ProductOfferingCreate.RefId("2"))));

        assertThat(out.href()).isEqualTo("/tmf-api/productCatalogManagement/v4/productOffering/10");
        assertThat(out.type()).isEqualTo("ProductOffering");
        assertThat(out.productSpecification().id()).isEqualTo("3");
        assertThat(out.productSpecification().referredType()).isEqualTo("ProductSpecification");
        assertThat(out.category()).hasSize(1);
        assertThat(out.category().get(0).href())
                .isEqualTo("/tmf-api/productCatalogManagement/v4/category/2");
    }

    @Test
    void create_unknownSpecRef_throwsInvalidInput() {
        when(specs.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new ProductOfferingCreate(
                "X", null, null, null, null, null,
                new ProductOfferingCreate.RefId("99"), null)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("productSpecification");
    }

    @Test
    void patch_appliesMergeSemantics_nullClearsDescription() {
        ProductOffering existing = saved(5, "Mobile 5G", "Active");
        existing.setDescription("old");
        when(offerings.findById(5L)).thenReturn(Optional.of(existing));
        when(offerings.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductOfferingTmf out = service.patch(5L, JsonMapper.builder().build()
                .readTree("{\"description\":null,\"lifecycleStatus\":\"Launched\"}"));

        assertThat(out.description()).isNull();
        assertThat(out.lifecycleStatus()).isEqualTo("Launched");
        assertThat(out.name()).isEqualTo("Mobile 5G");
    }

    @Test
    void patch_nullName_throwsInvalidInput() {
        ProductOffering existing = saved(5, "Mobile 5G", "Active");
        when(offerings.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.patch(5L, JsonMapper.builder().build()
                .readTree("{\"name\":null}")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void get_unknownId_throwsNotFound() {
        when(offerings.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void list_filtersByLifecycleStatusAndWindows() {
        when(offerings.findAll()).thenReturn(List.of(
                saved(1, "A", "Active"), saved(2, "B", "Retired"), saved(3, "C", "Active")));

        PageWindow<ProductOfferingTmf> page = service.list(null, "Active", 0, 1);

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).name()).isEqualTo("A");
    }
}
