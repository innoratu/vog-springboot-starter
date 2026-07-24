package com.vog.example.vog_tmf.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.tmf.TimePeriod;
import com.vog.example.vog_tmf.tmf.TmfApi;

/** TMF620 product specification representation (the "resource envelope" + payload). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSpecificationTmf(
        String id,
        String href,
        @JsonProperty("@type") String type,
        String name,
        String description,
        String brand,
        String version,
        String lifecycleStatus,
        TimePeriod validFor,
        Instant lastUpdate) {

    public static ProductSpecificationTmf from(ProductSpecification entity) {
        String id = String.valueOf(entity.getId());
        return new ProductSpecificationTmf(
                id,
                TmfApi.BASE_PATH + "/productSpecification/" + id,
                "ProductSpecification",
                entity.getName(),
                entity.getDescription(),
                entity.getBrand(),
                entity.getVersion(),
                entity.getLifecycleStatus(),
                entity.getValidFor(),
                entity.getLastUpdate());
    }
}
