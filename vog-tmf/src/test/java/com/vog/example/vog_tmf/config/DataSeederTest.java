package com.vog.example.vog_tmf.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;

@SpringBootTest
class DataSeederTest {

    @Autowired
    CategoryRepository categories;

    @Autowired
    ProductSpecificationRepository specs;

    @Autowired
    ProductOfferingRepository offerings;

    @Test
    void seedsCatalogOnStartup() {
        assertThat(categories.count()).isEqualTo(3);
        assertThat(specs.count()).isEqualTo(2);
        assertThat(offerings.count()).isEqualTo(4);
    }
}
