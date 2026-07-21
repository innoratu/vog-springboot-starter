package com.vog.example.vog_demo.config;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.entity.Organism;
import com.vog.example.vog_demo.repository.CategoryRepository;
import com.vog.example.vog_demo.repository.OrganismRepository;

/**
 * Seeds a starter set of categories and organisms on startup when the database
 * is empty, so the UI has data to show immediately.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(CategoryRepository categories, OrganismRepository organisms) {
        return args -> {
            if (categories.count() > 0) {
                return;
            }

            Category mammal = categories.save(new Category("Mammal", "Warm-blooded vertebrates with hair or fur"));
            Category fish = categories.save(new Category("Fish", "Gill-bearing aquatic animals"));
            Category bird = categories.save(new Category("Bird", "Feathered, egg-laying vertebrates"));
            Category plant = categories.save(new Category("Plant", "Multicellular photosynthetic organisms"));
            Category human = categories.save(new Category("Human", "Homo sapiens"));
            categories.save(new Category("Reptile", "Cold-blooded scaly vertebrates"));
            categories.save(new Category("Amphibian", "Cold-blooded vertebrates living in water and on land"));
            categories.save(new Category("Insect", "Six-legged invertebrates"));

            Map<Category, String[]> seed = Map.of(
                    mammal, new String[] {"Blue Whale", "Balaenoptera musculus", "Ocean"},
                    fish, new String[] {"Clownfish", "Amphiprioninae", "Coral reef"},
                    bird, new String[] {"Bald Eagle", "Haliaeetus leucocephalus", "Forests near water"},
                    plant, new String[] {"Sunflower", "Helianthus annuus", "Open fields"},
                    human, new String[] {"Human", "Homo sapiens", "Worldwide"});

            seed.forEach((category, data) -> {
                Organism organism = new Organism();
                organism.setCommonName(data[0]);
                organism.setScientificName(data[1]);
                organism.setHabitat(data[2]);
                organism.setCategory(category);
                organisms.save(organism);
            });
        };
    }
}
