package com.opsflow.opsflow_backend.application.catalog;

import com.opsflow.opsflow_backend.api.catalog.CatalogResponseDto;
import com.opsflow.opsflow_backend.api.catalog.CreateCatalogDto;
import com.opsflow.opsflow_backend.domain.catalog.Catalog;
import com.opsflow.opsflow_backend.infrastructure.persistence.catalog.CatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogServiceTest {

    private CatalogRepository catalogRepository;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogRepository = mock(CatalogRepository.class);
        catalogService = new CatalogService(catalogRepository);
    }

    private static void setId(Catalog catalog, Long id) {
        try {
            Field field = Catalog.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(catalog, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getByCategory_shouldReturnMappedDtos() {
        Catalog c1 = new Catalog("A1", "hardware", "Portatil", true);
        Catalog c2 = new Catalog("A2", "hardware", "Monitor", true);
        setId(c1, 1L);
        setId(c2, 2L);

        when(catalogRepository.findByCategoryIgnoreCaseAndActiveTrueOrderByCodeAsc("hardware"))
                .thenReturn(List.of(c1, c2));

        List<CatalogResponseDto> result = catalogService.getByCategory("hardware");

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).code());
        assertEquals("hardware", result.get(0).category());
        assertEquals("Portatil", result.get(0).description());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldSaveAndReturnDto_whenNotExists() {
        CreateCatalogDto dto = new CreateCatalogDto(" A1 ", " hardware ", " Portatil ", true);

        when(catalogRepository.existsByCategoryIgnoreCaseAndCodeIgnoreCase(" hardware ", " A1 "))
                .thenReturn(false);

        when(catalogRepository.save(any(Catalog.class))).thenAnswer(invocation -> {
            Catalog saved = invocation.getArgument(0);
            setId(saved, 10L);
            return saved;
        });

        CatalogResponseDto result = catalogService.create(dto);

        assertEquals("A1", result.code());
        assertEquals("hardware", result.category());
        assertEquals("Portatil", result.description());
        assertTrue(result.active());

        verify(catalogRepository).save(any(Catalog.class));
    }

    @Test
    void create_shouldThrowException_whenEntryAlreadyExists() {
        CreateCatalogDto dto = new CreateCatalogDto("A1", "hardware", "Portatil", true);

        when(catalogRepository.existsByCategoryIgnoreCaseAndCodeIgnoreCase("hardware", "A1"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> catalogService.create(dto)
        );

        assertEquals("Catalog entry already exists", ex.getMessage());
        verify(catalogRepository, never()).save(any());
    }
}