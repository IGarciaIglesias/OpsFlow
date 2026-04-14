package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseDtoTest {

    @Test
    void from_shouldMapRequest() {
        Request r = new Request("t", "desc ok");

        RequestResponseDto dto = RequestResponseDto.from(r);

        assertEquals("t", dto.title());
        assertEquals("desc ok", dto.description());
        assertEquals(RequestStatus.DRAFT, dto.status());
        assertNotNull(dto.createdAt());
    }

    @Test
    void historyDto_from_shouldMapHistory() {
        Request r = new Request("t", "desc ok");
        RequestHistory h = new RequestHistory(r, RequestStatus.DRAFT, RequestStatus.VALIDATED);

        RequestResponseDto.RequestHistoryDto dto = RequestResponseDto.RequestHistoryDto.from(h);

        assertEquals("DRAFT", dto.fromStatus());
        assertEquals("VALIDATED", dto.toStatus());
        assertNotNull(dto.changedAt());
    }
}