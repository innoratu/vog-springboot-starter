package com.vog.example.vog_tmf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.tmf.TmfApi;

public record CategoryRef(
        String id,
        String href,
        String name,
        @JsonProperty("@referredType") String referredType) {

    public static CategoryRef from(Category entity) {
        String id = String.valueOf(entity.getId());
        return new CategoryRef(id, TmfApi.BASE_PATH + "/category/" + id, entity.getName(), "Category");
    }
}
