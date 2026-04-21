package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
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

    private static void setStatus(Request r, RequestStatus status) {
        try {
            Field f = Request.class.getDeclaredField("status");
            f.setAccessible(true);
            f.set(r, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenRequestIsNotPending_consumerDoesNothing() {
        Request r = new Request("ok title", "desc ok ok ok");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        consumer.consume(RequestValidationMessage.of(1L, "msg-1", "corr-1"));

        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void whenValid_movesToValidatedAndSavesHistory() {
        Request r = new Request("Titulo OK", "Descripcion suficientemente larga para pasar validacion");
        setStatus(r, RequestStatus.PENDING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestValidationMessage.of(1L, "msg-2", "corr-2"));

        assertEquals(RequestStatus.VALIDATED, r.getStatus());
        verify(requestRepository).save(r);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository).save(captor.capture());

        RequestHistory savedHistory = captor.getValue();
        assertEquals(RequestStatus.PENDING, savedHistory.getFromStatus());
        assertEquals(RequestStatus.VALIDATED, savedHistory.getToStatus());
        assertNotNull(savedHistory.getChangedAt());
    }

    @Test
    void whenSpamDetected_movesToRejectedAndSavesHistory() {
        Request r = new Request("buy now", "Descripcion suficientemente larga para pasar validacion");
        setStatus(r, RequestStatus.PENDING);

        when(requestRepository.findById(2L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestValidationMessage.of(2L, "msg-3", "corr-3"));

        assertEquals(RequestStatus.REJECTED, r.getStatus());
        verify(requestRepository).save(r);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository).save(captor.capture());

        RequestHistory savedHistory = captor.getValue();
        assertEquals(RequestStatus.PENDING, savedHistory.getFromStatus());
        assertEquals(RequestStatus.REJECTED, savedHistory.getToStatus());
    }

    @Test
    void whenDescriptionIsOnlyNoise_movesToRejectedAndSavesHistory() {
        Request r = new Request("Titulo OK", "!!!");
        setStatus(r, RequestStatus.PENDING);

        when(requestRepository.findById(3L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestValidationMessage.of(3L, "msg-4", "corr-4"));

        assertEquals(RequestStatus.REJECTED, r.getStatus());
        verify(requestRepository).save(r);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository).save(captor.capture());

        RequestHistory savedHistory = captor.getValue();
        assertEquals(RequestStatus.PENDING, savedHistory.getFromStatus());
        assertEquals(RequestStatus.REJECTED, savedHistory.getToStatus());
    }

    @Test
    void whenRequestDoesNotExist_throwException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(RequestValidationMessage.of(99L, "msg-5", "corr-5"))
        );

        assertTrue(ex.getMessage().contains("Request not found"));
    }
}