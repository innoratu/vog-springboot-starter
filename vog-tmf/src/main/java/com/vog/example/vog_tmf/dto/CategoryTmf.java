package com.vog.example.vog_tmf.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.tmf.TimePeriod;
import com.vog.example.vog_tmf.tmf.TmfApi;

/** TMF620 category representation (the "resource envelope" + payload). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryTmf(
        String id,
        String href,
        @JsonProperty("@type") String type,
        String name,
        String description,
        String lifecycleStatus,
        Boolean isRoot,
        String parentId,
        TimePeriod validFor,
        Instant lastUpdate) {

    public static CategoryTmf from(Category entity) {
        String id = String.valueOf(entity.getId());
        return new CategoryTmf(
                id,
                TmfApi.BASE_PATH + "/category/" + id,
                "Category",
                entity.getName(),
                entity.getDescription(),
                entity.getLifecycleStatus(),
                entity.getIsRoot(),
                entity.getParent() == null ? null : String.valueOf(entity.getParent().getId()),
                entity.getValidFor(),
                entity.getLastUpdate());
    }
}
