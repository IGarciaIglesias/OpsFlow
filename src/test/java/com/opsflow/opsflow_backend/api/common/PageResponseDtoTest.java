package com.opsflow.opsflow_backend.api.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseDtoTest {

    @Test
    void from_shouldMapPageMetadataAndContent() {
        Page<String> page = new PageImpl<>(
                List.of("a", "b"),
                PageRequest.of(1, 2),
                5
        );

        PageResponseDto<String> dto = PageResponseDto.from(page);

        assertEquals(List.of("a", "b"), dto.content());
        assertEquals(1, dto.page());
        assertEquals(2, dto.size());
        assertEquals(5, dto.totalElements());
        assertEquals(3, dto.totalPages());
        assertFalse(dto.first());
        assertFalse(dto.last());
    }
}