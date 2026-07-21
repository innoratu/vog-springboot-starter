package com.vog.example.vog_demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vog.example.vog_demo.dto.CategoryRequest;
import com.vog.example.vog_demo.dto.CategoryResponse;
import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.exception.InUseException;
import com.vog.example.vog_demo.exception.NotFoundException;
import com.vog.example.vog_demo.repository.CategoryRepository;
import com.vog.example.vog_demo.repository.OrganismRepository;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final OrganismRepository organismRepository;

    public CategoryService(CategoryRepository categoryRepository, OrganismRepository organismRepository) {
        this.categoryRepository = categoryRepository;
        this.organismRepository = organismRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return CategoryResponse.from(findOrThrow(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new InUseException("Category name already exists: " + request.name());
        }
        Category category = new Category(request.name(), request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);
        category.setName(request.name());
        category.setDescription(request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = findOrThrow(id);
        if (organismRepository.existsByCategoryId(id)) {
            throw new InUseException("Cannot delete category '" + category.getName()
                    + "' because organisms still reference it");
        }
        categoryRepository.delete(category);
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }
}
