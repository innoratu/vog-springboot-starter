package com.vog.example.vog_demo.service;

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

import com.vog.example.vog_demo.dto.OrganismRequest;
import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.entity.Organism;
import com.vog.example.vog_demo.exception.NotFoundException;
import com.vog.example.vog_demo.repository.CategoryRepository;
import com.vog.example.vog_demo.repository.OrganismRepository;

@ExtendWith(MockitoExtension.class)
class OrganismServiceTest {

    @Mock
    OrganismRepository organismRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    OrganismService service;

    @Test
    void create_unknownCategory_throwsNotFound() {
        OrganismRequest request = new OrganismRequest("Whale", null, null, null, 42L);
        when(categoryRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_validCategory_savesAndMaps() {
        Category mammal = new Category("Mammal", null);
        mammal.setId(1L);
        OrganismRequest request = new OrganismRequest("Whale", "Balaenoptera", "Ocean", null, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mammal));
        when(organismRepository.save(any(Organism.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(request).commonName()).isEqualTo("Whale");
        assertThat(service.create(request).categoryName()).isEqualTo("Mammal");
    }

    @Test
    void list_byCategoryId_delegatesToCategoryFinder() {
        Category mammal = new Category("Mammal", null);
        mammal.setId(1L);
        Organism whale = new Organism();
        whale.setCommonName("Whale");
        whale.setCategory(mammal);
        when(organismRepository.findByCategoryId(1L)).thenReturn(List.of(whale));

        assertThat(service.list(1L, null)).singleElement()
                .satisfies(r -> assertThat(r.commonName()).isEqualTo("Whale"));
    }
}
