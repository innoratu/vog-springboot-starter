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

import com.vog.example.vog_tmf.dto.ProductSpecificationCreate;
import com.vog.example.vog_tmf.dto.ProductSpecificationTmf;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.exception.InUseException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationServiceTest {

    @Mock
    ProductSpecificationRepository specs;

    @Mock
    ProductOfferingRepository offerings;

    @InjectMocks
    ProductSpecificationService service;

    private ProductSpecification saved(long id, String name, String status) {
        ProductSpecification p = new ProductSpecification();
        p.setId(id);
        p.setName(name);
        p.setLifecycleStatus(status);
        return p;
    }

    @Test
    void create_buildsHrefAndTypeAndDefaultsLifecycle() {
        when(specs.save(any())).thenAnswer(inv -> {
            ProductSpecification p = inv.getArgument(0);
            p.setId(7L);
            return p;
        });

        ProductSpecificationTmf out = service.create(
                new ProductSpecificationCreate("5G Plan", null, null, null, null, null));

        assertThat(out.href()).isEqualTo("/tmf-api/productCatalogManagement/v4/productSpecification/7");
        assertThat(out.type()).isEqualTo("ProductSpecification");
        assertThat(out.lifecycleStatus()).isEqualTo("In study");
    }

    @Test
    void list_filtersByLifecycleStatusAndWindows() {
        when(specs.findAll()).thenReturn(List.of(
                saved(1, "A", "Active"), saved(2, "B", "Retired"), saved(3, "C", "Active")));

        PageWindow<ProductSpecificationTmf> page = service.list(null, "Active", 0, 1);

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).name()).isEqualTo("A");
    }

    @Test
    void patch_appliesMergeSemantics_nullClearsField() {
        ProductSpecification existing = saved(5, "Mobile", "Active");
        existing.setDescription("old");
        when(specs.findById(5L)).thenReturn(Optional.of(existing));
        when(specs.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductSpecificationTmf out = service.patch(5L, JsonMapper.builder().build()
                .readTree("{\"description\":null,\"lifecycleStatus\":\"Launched\"}"));

        assertThat(out.description()).isNull();
        assertThat(out.lifecycleStatus()).isEqualTo("Launched");
        assertThat(out.name()).isEqualTo("Mobile");
    }

    @Test
    void get_unknownId_throwsNotFound() {
        when(specs.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_referencedByOffering_throwsInUse() {
        when(specs.findById(5L)).thenReturn(Optional.of(saved(5, "Mobile", "Active")));
        when(offerings.existsByProductSpecificationId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(InUseException.class);
    }
}
