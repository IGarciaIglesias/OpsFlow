package com.opsflow.opsflow_backend.domain.request.listener;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.event.RequestApprovedEvent;
import com.opsflow.opsflow_backend.domain.request.event.RequestRejectedEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RequestDomainEventListenerTest {

    private static void setId(Request request, long id) {
        try {
            Field field = Request.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(request, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void onApproved_shouldNotThrow() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 1L);

        RequestApprovedEvent event = new RequestApprovedEvent(request);

        RequestDomainEventListener listener = new RequestDomainEventListener();
        assertDoesNotThrow(() -> listener.onApproved(event));
    }

    @Test
    void onRejected_shouldNotThrow() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 2L);

        RequestRejectedEvent event = new RequestRejectedEvent(request);

        RequestDomainEventListener listener = new RequestDomainEventListener();
        assertDoesNotThrow(() -> listener.onRejected(event));
    }
}