package com.vog.example.vog_tmf.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vog.example.vog_tmf.dto.ProductSpecificationCreate;
import com.vog.example.vog_tmf.dto.ProductSpecificationTmf;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.exception.InvalidInputException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import java.util.List;

import tools.jackson.databind.JsonNode;

@Service
@Transactional
public class ProductSpecificationService {

    private final ProductSpecificationRepository specs;

    public ProductSpecificationService(ProductSpecificationRepository specs) {
        this.specs = specs;
    }

    @Transactional(readOnly = true)
    public PageWindow<ProductSpecificationTmf> list(String name, String lifecycleStatus, int offset, int limit) {
        List<ProductSpecificationTmf> matching = specs.findAll().stream()
                .filter(p -> name == null || name.equals(p.getName()))
                .filter(p -> lifecycleStatus == null || lifecycleStatus.equals(p.getLifecycleStatus()))
                .map(ProductSpecificationTmf::from)
                .toList();
        List<ProductSpecificationTmf> window = matching.stream().skip(offset).limit(limit).toList();
        return new PageWindow<>(window, matching.size());
    }

    @Transactional(readOnly = true)
    public ProductSpecificationTmf get(Long id) {
        return ProductSpecificationTmf.from(findOrThrow(id));
    }

    public ProductSpecificationTmf create(ProductSpecificationCreate request) {
        ProductSpecification entity = new ProductSpecification();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setBrand(request.brand());
        entity.setVersion(request.version());
        entity.setLifecycleStatus(request.lifecycleStatus() == null ? "In study" : request.lifecycleStatus());
        entity.setValidFor(request.validFor());
        return ProductSpecificationTmf.from(specs.save(entity));
    }

    /** JSON Merge Patch (RFC 7386): absent = keep, null = clear, value = replace. */
    public ProductSpecificationTmf patch(Long id, JsonNode patch) {
        ProductSpecification entity = findOrThrow(id);
        if (patch.has("name")) {
            if (patch.get("name").isNull()) {
                throw new InvalidInputException("name is mandatory and cannot be removed");
            }
            entity.setName(patch.get("name").asString());
        }
        if (patch.has("description")) {
            entity.setDescription(patch.get("description").isNull() ? null : patch.get("description").asString());
        }
        if (patch.has("brand")) {
            entity.setBrand(patch.get("brand").isNull() ? null : patch.get("brand").asString());
        }
        if (patch.has("version")) {
            entity.setVersion(patch.get("version").isNull() ? null : patch.get("version").asString());
        }
        if (patch.has("lifecycleStatus")) {
            entity.setLifecycleStatus(patch.get("lifecycleStatus").isNull() ? null : patch.get("lifecycleStatus").asString());
        }
        return ProductSpecificationTmf.from(specs.save(entity));
    }

    public void delete(Long id) {
        specs.delete(findOrThrow(id));
    }

    private ProductSpecification findOrThrow(Long id) {
        return specs.findById(id)
                .orElseThrow(() -> new NotFoundException("ProductSpecification not found: " + id));
    }
}
