package com.opsflow.opsflow_backend.api.catalog;

import com.opsflow.opsflow_backend.domain.catalog.Catalog;

public record CatalogResponseDto(
        Long id,
        String code,
        String category,
        String description,
        boolean active
) {
    public static CatalogResponseDto from(Catalog catalog) {
        return new CatalogResponseDto(
                catalog.getId(),
                catalog.getCode(),
                catalog.getCategory(),
                catalog.getDescription(),
                catalog.isActive()
        );
    }
}