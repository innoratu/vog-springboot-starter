package com.vog.example.vog_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrganismRequest(
        @NotBlank(message = "commonName is required") String commonName,
        String scientificName,
        String habitat,
        String description,
        @NotNull(message = "categoryId is required") Long categoryId) {
}
