package com.awbd.financetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpsertDto(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must be less than 100 characters")
        String name,
        @Size(max = 500, message = "Description must be less than 500 characters")
        String description
) {
}
