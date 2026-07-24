package com.vog.example.vog_tmf.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vog.example.vog_tmf.entity.ProductOffering;
import com.vog.example.vog_tmf.tmf.TimePeriod;
import com.vog.example.vog_tmf.tmf.TmfApi;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductOfferingTmf(
        String id,
        String href,
        @JsonProperty("@type") String type,
        String name,
        String description,
        Boolean isBundle,
        Boolean isSellable,
        String lifecycleStatus,
        TimePeriod validFor,
        Instant lastUpdate,
        ProductSpecificationRef productSpecification,
        List<CategoryRef> category) {

    public static ProductOfferingTmf from(ProductOffering entity) {
        String id = String.valueOf(entity.getId());
        return new ProductOfferingTmf(
                id,
                TmfApi.BASE_PATH + "/productOffering/" + id,
                "ProductOffering",
                entity.getName(),
                entity.getDescription(),
                entity.getIsBundle(),
                entity.getIsSellable(),
                entity.getLifecycleStatus(),
                entity.getValidFor(),
                entity.getLastUpdate(),
                entity.getProductSpecification() == null
                        ? null
                        : ProductSpecificationRef.from(entity.getProductSpecification()),
                entity.getCategories().isEmpty()
                        ? null
                        : entity.getCategories().stream().map(CategoryRef::from).toList());
    }
}
