package com.opsflow.opsflow_backend.application.catalog;

import com.opsflow.opsflow_backend.api.catalog.CatalogResponseDto;
import com.opsflow.opsflow_backend.api.catalog.CreateCatalogDto;
import com.opsflow.opsflow_backend.domain.catalog.Catalog;
import com.opsflow.opsflow_backend.infrastructure.persistence.catalog.CatalogRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Cacheable(value = "catalogsByCategory", key = "#category.toLowerCase()")
    public List<CatalogResponseDto> getByCategory(String category) {
        return catalogRepository.findByCategoryIgnoreCaseAndActiveTrueOrderByCodeAsc(category)
                .stream()
                .map(CatalogResponseDto::from)
                .toList();
    }

    @CacheEvict(value = "catalogsByCategory", key = "#dto.category().toLowerCase()")
    public CatalogResponseDto create(CreateCatalogDto dto) {
        boolean exists = catalogRepository.existsByCategoryIgnoreCaseAndCodeIgnoreCase(
                dto.category(),
                dto.code()
        );

        if (exists) {
            throw new IllegalArgumentException("Catalog entry already exists");
        }

        Catalog catalog = new Catalog(
                dto.code().trim(),
                dto.category().trim(),
                dto.description().trim(),
                dto.active()
        );

        Catalog saved = catalogRepository.save(catalog);
        return CatalogResponseDto.from(saved);
    }
}