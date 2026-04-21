package com.opsflow.opsflow_backend.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHistoryTest {

    @Test
    void constructor_shouldSetFieldsAndTimestamp() {
        Request request = new Request("title", "description");
        RequestHistory history = new RequestHistory(request, RequestStatus.DRAFT, RequestStatus.PENDING);

        assertEquals(request, history.getRequest());
        assertEquals(RequestStatus.DRAFT, history.getFromStatus());
        assertEquals(RequestStatus.PENDING, history.getToStatus());
        assertNotNull(history.getChangedAt());
    }
}