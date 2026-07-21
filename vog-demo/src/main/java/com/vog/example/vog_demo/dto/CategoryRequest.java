package com.vog.example.vog_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "name is required") String name,
        @Size(max = 500, message = "description must be at most 500 characters") String description) {
}
