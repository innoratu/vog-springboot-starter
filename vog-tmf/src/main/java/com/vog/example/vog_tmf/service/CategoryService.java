package com.vog.example.vog_tmf.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vog.example.vog_tmf.dto.CategoryCreate;
import com.vog.example.vog_tmf.dto.CategoryTmf;
import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.exception.InUseException;
import com.vog.example.vog_tmf.exception.InvalidInputException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import java.util.List;

import tools.jackson.databind.JsonNode;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductOfferingRepository offerings;

    public CategoryService(CategoryRepository categories, ProductOfferingRepository offerings) {
        this.categories = categories;
        this.offerings = offerings;
    }

    @Transactional(readOnly = true)
    public PageWindow<CategoryTmf> list(String name, String lifecycleStatus, int offset, int limit) {
        List<CategoryTmf> matching = categories.findAll().stream()
                .filter(c -> name == null || name.equals(c.getName()))
                .filter(c -> lifecycleStatus == null || lifecycleStatus.equals(c.getLifecycleStatus()))
                .map(CategoryTmf::from)
                .toList();
        List<CategoryTmf> window = matching.stream().skip(offset).limit(limit).toList();
        return new PageWindow<>(window, matching.size());
    }

    @Transactional(readOnly = true)
    public CategoryTmf get(Long id) {
        return CategoryTmf.from(findOrThrow(id));
    }

    public CategoryTmf create(CategoryCreate request) {
        Category entity = new Category();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setLifecycleStatus(request.lifecycleStatus() == null ? "In study" : request.lifecycleStatus());
        entity.setIsRoot(request.isRoot() == null ? request.parentId() == null : request.isRoot());
        entity.setValidFor(request.validFor());
        if (request.parentId() != null) {
            entity.setParent(resolveParent(request.parentId()));
        }
        return CategoryTmf.from(categories.save(entity));
    }

    /** JSON Merge Patch (RFC 7386): absent = keep, null = clear, value = replace. */
    public CategoryTmf patch(Long id, JsonNode patch) {
        Category entity = findOrThrow(id);
        if (patch.has("name")) {
            if (patch.get("name").isNull()) {
                throw new InvalidInputException("name is mandatory and cannot be removed");
            }
            entity.setName(patch.get("name").asString());
        }
        if (patch.has("description")) {
            entity.setDescription(patch.get("description").isNull() ? null : patch.get("description").asString());
        }
        if (patch.has("lifecycleStatus")) {
            entity.setLifecycleStatus(patch.get("lifecycleStatus").isNull() ? null : patch.get("lifecycleStatus").asString());
        }
        if (patch.has("isRoot")) {
            entity.setIsRoot(patch.get("isRoot").isNull() ? null : patch.get("isRoot").asBoolean());
        }
        return CategoryTmf.from(categories.save(entity));
    }

    public void delete(Long id) {
        Category entity = findOrThrow(id);
        if (offerings.existsByCategoriesId(id)) {
            throw new InUseException("Category " + id + " is referenced by product offerings");
        }
        categories.delete(entity);
    }

    private Category resolveParent(String parentId) {
        long parsed = parseId(parentId, "parentId");
        return categories.findById(parsed)
                .orElseThrow(() -> new InvalidInputException("Unknown parentId: " + parentId));
    }

    static long parseId(String raw, String fieldName) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid " + fieldName + ": " + raw);
        }
    }

    private Category findOrThrow(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }
}
