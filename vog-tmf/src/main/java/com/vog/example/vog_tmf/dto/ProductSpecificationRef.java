package com.vog.example.vog_tmf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.tmf.TmfApi;

public record ProductSpecificationRef(
        String id,
        String href,
        String name,
        @JsonProperty("@referredType") String referredType) {

    public static ProductSpecificationRef from(ProductSpecification entity) {
        String id = String.valueOf(entity.getId());
        return new ProductSpecificationRef(id, TmfApi.BASE_PATH + "/productSpecification/" + id,
                entity.getName(), "ProductSpecification");
    }
}
