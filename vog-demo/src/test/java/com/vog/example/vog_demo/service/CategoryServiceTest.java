package com.vog.example.vog_demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.exception.InUseException;
import com.vog.example.vog_demo.exception.NotFoundException;
import com.vog.example.vog_demo.repository.CategoryRepository;
import com.vog.example.vog_demo.repository.OrganismRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    OrganismRepository organismRepository;

    @InjectMocks
    CategoryService service;

    @Test
    void get_unknownId_throwsNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_categoryInUse_throwsInUse() {
        Category category = new Category("Mammal", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(organismRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(InUseException.class);
    }

    @Test
    void delete_categoryNotInUse_deletes() {
        Category category = new Category("Mammal", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(organismRepository.existsByCategoryId(1L)).thenReturn(false);

        service.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void create_duplicateName_throwsInUse() {
        when(categoryRepository.existsByName("Mammal")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new com.vog.example.vog_demo.dto.CategoryRequest("Mammal", null)))
                .isInstanceOf(InUseException.class);
    }

    @Test
    void list_returnsMappedResponses() {
        when(categoryRepository.findAll()).thenReturn(java.util.List.of(new Category("Fish", "desc")));

        assertThat(service.list()).singleElement()
                .satisfies(r -> assertThat(r.name()).isEqualTo("Fish"));
    }
}
