package com.opsflow.opsflow_backend.infrastructure.persistence.catalog;

import com.opsflow.opsflow_backend.domain.catalog.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    List<Catalog> findByCategoryIgnoreCaseAndActiveTrueOrderByCodeAsc(String category);

    boolean existsByCategoryIgnoreCaseAndCodeIgnoreCase(String category, String code);
}