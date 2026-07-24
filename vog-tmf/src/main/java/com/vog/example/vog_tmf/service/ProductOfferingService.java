package com.vog.example.vog_tmf.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vog.example.vog_tmf.dto.ProductOfferingCreate;
import com.vog.example.vog_tmf.dto.ProductOfferingTmf;
import com.vog.example.vog_tmf.entity.ProductOffering;
import com.vog.example.vog_tmf.exception.InvalidInputException;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;
import com.vog.example.vog_tmf.tmf.PageWindow;

import tools.jackson.databind.JsonNode;

@Service
@Transactional
public class ProductOfferingService {

    private final ProductOfferingRepository offerings;
    private final ProductSpecificationRepository specs;
    private final CategoryRepository categories;

    public ProductOfferingService(ProductOfferingRepository offerings,
            ProductSpecificationRepository specs, CategoryRepository categories) {
        this.offerings = offerings;
        this.specs = specs;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public PageWindow<ProductOfferingTmf> list(String name, String lifecycleStatus, int offset, int limit) {
        List<ProductOfferingTmf> matching = offerings.findAll().stream()
                .filter(o -> name == null || name.equals(o.getName()))
                .filter(o -> lifecycleStatus == null || lifecycleStatus.equals(o.getLifecycleStatus()))
                .map(ProductOfferingTmf::from)
                .toList();
        List<ProductOfferingTmf> window = matching.stream().skip(offset).limit(limit).toList();
        return new PageWindow<>(window, matching.size());
    }

    @Transactional(readOnly = true)
    public ProductOfferingTmf get(Long id) {
        return ProductOfferingTmf.from(findOrThrow(id));
    }

    public ProductOfferingTmf create(ProductOfferingCreate request) {
        ProductOffering entity = new ProductOffering();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setIsBundle(request.isBundle() == null ? Boolean.FALSE : request.isBundle());
        entity.setIsSellable(request.isSellable() == null ? Boolean.TRUE : request.isSellable());
        entity.setLifecycleStatus(request.lifecycleStatus() == null ? "In study" : request.lifecycleStatus());
        entity.setValidFor(request.validFor());
        if (request.productSpecification() != null) {
            long specId = CategoryService.parseId(request.productSpecification().id(), "productSpecification.id");
            entity.setProductSpecification(specs.findById(specId)
                    .orElseThrow(() -> new InvalidInputException("Unknown productSpecification id: " + specId)));
        }
        if (request.category() != null) {
            for (ProductOfferingCreate.RefId ref : request.category()) {
                long categoryId = CategoryService.parseId(ref.id(), "category.id");
                entity.getCategories().add(categories.findById(categoryId)
                        .orElseThrow(() -> new InvalidInputException("Unknown category id: " + categoryId)));
            }
        }
        return ProductOfferingTmf.from(offerings.save(entity));
    }

    /** JSON Merge Patch for scalar fields; ref fields are not patchable in this slice. */
    public ProductOfferingTmf patch(Long id, JsonNode patch) {
        ProductOffering entity = findOrThrow(id);
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
        if (patch.has("isSellable")) {
            entity.setIsSellable(patch.get("isSellable").isNull() ? null : patch.get("isSellable").asBoolean());
        }
        if (patch.has("isBundle")) {
            entity.setIsBundle(patch.get("isBundle").isNull() ? null : patch.get("isBundle").asBoolean());
        }
        return ProductOfferingTmf.from(offerings.save(entity));
    }

    public void delete(Long id) {
        offerings.delete(findOrThrow(id));
    }

    private ProductOffering findOrThrow(Long id) {
        return offerings.findById(id)
                .orElseThrow(() -> new NotFoundException("ProductOffering not found: " + id));
    }
}
