package com.vog.example.vog_tmf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.vog.example.vog_tmf.entity.Category;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categories;

    @Test
    void savesSelfReferencingParentAndAutoStampsLastUpdate() {
        Category root = new Category();
        root.setName("Mobile");
        root.setLifecycleStatus("Active");
        root.setIsRoot(true);
        Category savedRoot = categories.save(root);

        Category child = new Category();
        child.setName("Mobile Postpaid");
        child.setLifecycleStatus("Active");
        child.setIsRoot(false);
        child.setParent(savedRoot);
        Category savedChild = categories.save(child);

        assertThat(savedChild.getParent().getId()).isEqualTo(savedRoot.getId());
        assertThat(savedChild.getLastUpdate()).isNotNull();
    }
}
