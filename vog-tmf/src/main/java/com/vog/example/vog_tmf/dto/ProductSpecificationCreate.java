package com.vog.example.vog_tmf.dto;

import com.vog.example.vog_tmf.tmf.TimePeriod;

import jakarta.validation.constraints.NotBlank;

public record ProductSpecificationCreate(
        @NotBlank String name,
        String description,
        String brand,
        String version,
        String lifecycleStatus,
        TimePeriod validFor) {
}
