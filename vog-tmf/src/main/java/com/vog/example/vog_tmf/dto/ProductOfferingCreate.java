package com.vog.example.vog_tmf.dto;

import java.util.List;

import com.vog.example.vog_tmf.tmf.TimePeriod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record ProductOfferingCreate(
        @NotBlank String name,
        String description,
        Boolean isBundle,
        Boolean isSellable,
        String lifecycleStatus,
        TimePeriod validFor,
        @Valid RefId productSpecification,
        List<@Valid RefId> category) {

    public record RefId(@NotBlank String id) {
    }
}
