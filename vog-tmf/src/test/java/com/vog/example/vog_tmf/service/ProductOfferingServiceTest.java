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
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;

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
}
