package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestPriority;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.RequestType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseDtoTest {

    @Test
    void from_shouldMapRequest() {
        Request r = new Request(
                "t",
                "desc ok",
                "creator",
                "assignee",
                RequestPriority.HIGH,
                RequestType.INCIDENT
        );

        RequestResponseDto dto = RequestResponseDto.from(r);

        assertEquals("t", dto.title());
        assertEquals("desc ok", dto.description());
        assertEquals("creator", dto.creator());
        assertEquals("assignee", dto.assignee());
        assertEquals(RequestPriority.HIGH, dto.priority());
        assertEquals(RequestType.INCIDENT, dto.type());
        assertEquals(RequestStatus.DRAFT, dto.status());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.code());
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