package com.vog.example.vog_demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.entity.Organism;

@DataJpaTest
class OrganismRepositoryTest {

    @Autowired
    CategoryRepository categories;

    @Autowired
    OrganismRepository organisms;

    @Test
    void findByCategoryId_returnsOnlyMatchingOrganisms() {
        Category mammal = categories.save(new Category("Mammal", null));
        Category fish = categories.save(new Category("Fish", null));

        organisms.save(organism("Whale", mammal));
        organisms.save(organism("Bat", mammal));
        organisms.save(organism("Salmon", fish));

        List<Organism> result = organisms.findByCategoryId(mammal.getId());

        assertThat(result).extracting(Organism::getCommonName).containsExactlyInAnyOrder("Whale", "Bat");
    }

    @Test
    void findByCommonNameContainingIgnoreCase_matchesPartialCaseInsensitive() {
        Category mammal = categories.save(new Category("Mammal", null));
        organisms.save(organism("Blue Whale", mammal));

        assertThat(organisms.findByCommonNameContainingIgnoreCase("whale")).hasSize(1);
    }

    private Organism organism(String name, Category category) {
        Organism o = new Organism();
        o.setCommonName(name);
        o.setCategory(category);
        return o;
    }
}
