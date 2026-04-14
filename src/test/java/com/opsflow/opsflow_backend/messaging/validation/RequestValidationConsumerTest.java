package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestValidationConsumerTest {

    private RequestRepository requestRepository;
    private RequestHistoryRepository historyRepository;
    private RequestValidationConsumer consumer;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        historyRepository = mock(RequestHistoryRepository.class);
        consumer = new RequestValidationConsumer(requestRepository, historyRepository);
    }

    @Test
    void whenRequestIsNotValidated_consumerDoesNothing() {
        Request r = new Request("ok", "desc ok"); // DRAFT
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        consumer.consume(RequestValidationMessage.of(1L));

        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void whenValidatedAndValid_movesToPendingAndSavesHistory() {
        Request r = new Request("Titulo OK", "Descripcion OK");
        r.submit(); // VALIDATED

        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestValidationMessage.of(1L));

        assertEquals(RequestStatus.PENDING, r.getStatus());
        verify(requestRepository).save(r);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository).save(captor.capture());

        RequestHistory savedHistory = captor.getValue();
        assertEquals(RequestStatus.VALIDATED, savedHistory.getFromStatus());
        assertEquals(RequestStatus.PENDING, savedHistory.getToStatus());
        assertNotNull(savedHistory.getChangedAt());
    }

    @Test
    void whenValidatedButInvalid_movesToRejectedAndSavesHistory() {
        Request r = new Request("x", "y"); // inválido para la regla (title<3, desc<5)
        r.submit(); // VALIDATED

        when(requestRepository.findById(2L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        // ✅ AQUÍ estaba el bug: antes enviabas of(1L)
        consumer.consume(RequestValidationMessage.of(2L));

        assertEquals(RequestStatus.REJECTED, r.getStatus());
        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
    }
}