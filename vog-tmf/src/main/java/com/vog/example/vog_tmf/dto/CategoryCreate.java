package com.vog.example.vog_tmf.dto;

import com.vog.example.vog_tmf.tmf.TimePeriod;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreate(
        @NotBlank String name,
        String description,
        String lifecycleStatus,
        Boolean isRoot,
        String parentId,
        TimePeriod validFor) {
}
