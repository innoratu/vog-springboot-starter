package com.vog.example.vog_demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vog.example.vog_demo.dto.OrganismRequest;
import com.vog.example.vog_demo.dto.OrganismResponse;
import com.vog.example.vog_demo.entity.Category;
import com.vog.example.vog_demo.entity.Organism;
import com.vog.example.vog_demo.exception.NotFoundException;
import com.vog.example.vog_demo.repository.CategoryRepository;
import com.vog.example.vog_demo.repository.OrganismRepository;

@Service
@Transactional
public class OrganismService {

    private final OrganismRepository organismRepository;
    private final CategoryRepository categoryRepository;

    public OrganismService(OrganismRepository organismRepository, CategoryRepository categoryRepository) {
        this.organismRepository = organismRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganismResponse> list(Long categoryId, String name) {
        boolean hasName = StringUtils.hasText(name);
        List<Organism> organisms;
        if (categoryId != null && hasName) {
            organisms = organismRepository.findByCategoryIdAndCommonNameContainingIgnoreCase(categoryId, name);
        } else if (categoryId != null) {
            organisms = organismRepository.findByCategoryId(categoryId);
        } else if (hasName) {
            organisms = organismRepository.findByCommonNameContainingIgnoreCase(name);
        } else {
            organisms = organismRepository.findAll();
        }
        return organisms.stream().map(OrganismResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrganismResponse get(Long id) {
        return OrganismResponse.from(findOrThrow(id));
    }

    public OrganismResponse create(OrganismRequest request) {
        Organism organism = new Organism();
        apply(organism, request);
        return OrganismResponse.from(organismRepository.save(organism));
    }

    public OrganismResponse update(Long id, OrganismRequest request) {
        Organism organism = findOrThrow(id);
        apply(organism, request);
        return OrganismResponse.from(organismRepository.save(organism));
    }

    public void delete(Long id) {
        organismRepository.delete(findOrThrow(id));
    }

    private void apply(Organism organism, OrganismRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));
        organism.setCommonName(request.commonName());
        organism.setScientificName(request.scientificName());
        organism.setHabitat(request.habitat());
        organism.setDescription(request.description());
        organism.setCategory(category);
    }

    private Organism findOrThrow(Long id) {
        return organismRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organism not found: " + id));
    }
}
