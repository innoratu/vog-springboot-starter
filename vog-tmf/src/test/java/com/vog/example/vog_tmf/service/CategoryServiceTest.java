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

import com.vog.example.vog_tmf.dto.CategoryCreate;
import com.vog.example.vog_tmf.dto.CategoryTmf;
import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.exception.InUseException;
import com.vog.example.vog_tmf.exception.InvalidInputException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categories;

    @Mock
    ProductOfferingRepository offerings;

    @InjectMocks
    CategoryService service;

    private Category saved(long id, String name, String status) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setLifecycleStatus(status);
        return c;
    }

    @Test
    void create_buildsHrefAndTypeAndDefaultsLifecycle() {
        when(categories.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });

        CategoryTmf out = service.create(new CategoryCreate("Mobile", null, null, true, null, null));

        assertThat(out.href()).isEqualTo("/tmf-api/productCatalogManagement/v4/category/7");
        assertThat(out.type()).isEqualTo("Category");
        assertThat(out.lifecycleStatus()).isEqualTo("In study");
    }

    @Test
    void create_unknownParentId_throwsInvalidInput() {
        when(categories.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CategoryCreate("X", null, null, false, "99", null)))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void list_filtersByLifecycleStatusAndWindows() {
        when(categories.findAll()).thenReturn(List.of(
                saved(1, "A", "Active"), saved(2, "B", "Retired"), saved(3, "C", "Active")));

        PageWindow<CategoryTmf> page = service.list(null, "Active", 0, 1);

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).name()).isEqualTo("A");
    }

    @Test
    void patch_appliesMergeSemantics_nullClearsField() {
        Category existing = saved(5, "Mobile", "Active");
        existing.setDescription("old");
        when(categories.findById(5L)).thenReturn(Optional.of(existing));
        when(categories.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryTmf out = service.patch(5L, JsonMapper.builder().build()
                .readTree("{\"description\":null,\"lifecycleStatus\":\"Launched\"}"));

        assertThat(out.description()).isNull();
        assertThat(out.lifecycleStatus()).isEqualTo("Launched");
        assertThat(out.name()).isEqualTo("Mobile");
    }

    @Test
    void get_unknownId_throwsNotFound() {
        when(categories.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_referencedByOffering_throwsInUse() {
        when(categories.findById(5L)).thenReturn(Optional.of(saved(5, "Mobile", "Active")));
        when(offerings.existsByCategoriesId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(InUseException.class);
    }
}
