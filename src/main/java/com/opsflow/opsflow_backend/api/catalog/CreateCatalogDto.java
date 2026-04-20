package com.opsflow.opsflow_backend.api.catalog;

import jakarta.validation.constraints.NotBlank;

public record CreateCatalogDto(
        @NotBlank String code,
        @NotBlank String category,
        @NotBlank String description,
        boolean active
) {}