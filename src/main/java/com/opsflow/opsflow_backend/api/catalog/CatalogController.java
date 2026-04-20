package com.opsflow.opsflow_backend.api.catalog;

import com.opsflow.opsflow_backend.application.catalog.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<CatalogResponseDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(catalogService.getByCategory(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogResponseDto> create(
            @Valid @RequestBody CreateCatalogDto dto
    ) {
        CatalogResponseDto created = catalogService.create(dto);

        return ResponseEntity
                .created(URI.create("/catalogs/" + created.category()))
                .body(created);
    }
}