package com.opsflow.opsflow_backend.messaging.execution;

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

class RequestExecutionConsumerTest {

    private RequestRepository requestRepository;
    private RequestHistoryRepository historyRepository;
    private RequestExecutionConsumer consumer;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        historyRepository = mock(RequestHistoryRepository.class);
        consumer = new RequestExecutionConsumer(requestRepository, historyRepository);
    }

    private static void setId(Request r, long id) {
        try {
            Field f = Request.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void whenRequestDoesNotExist_throwException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        RequestExecutionMessage message = RequestExecutionMessage.of(99L, "REQ-99", "iago");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(message)
        );

        assertTrue(ex.getMessage().contains("Request not found"));
        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void whenRequestIsNotApproved_consumerDoesNothing() {
        Request request = new Request("Titulo", "descripcion suficiente");
        setId(request, 1L);
        setStatus(request, RequestStatus.PENDING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        consumer.consume(RequestExecutionMessage.of(1L, "REQ-1", "iago"));

        assertEquals(RequestStatus.PENDING, request.getStatus());
        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void whenExecutionSucceeds_movesToCompletedAndSavesHistoryTwice() {
        Request request = new Request("Titulo", "descripcion correcta");
        setId(request, 2L);
        setStatus(request, RequestStatus.APPROVED);

        when(requestRepository.findById(2L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestExecutionMessage.of(2L, "REQ-2", "iago"));

        assertEquals(RequestStatus.COMPLETED, request.getStatus());
        verify(requestRepository, times(2)).save(request);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository, times(2)).save(captor.capture());

        assertEquals(RequestStatus.APPROVED, captor.getAllValues().get(0).getFromStatus());
        assertEquals(RequestStatus.IN_PROGRESS, captor.getAllValues().get(0).getToStatus());
        assertEquals(RequestStatus.IN_PROGRESS, captor.getAllValues().get(1).getFromStatus());
        assertEquals(RequestStatus.COMPLETED, captor.getAllValues().get(1).getToStatus());
    }

    @Test
    void whenExecutionFails_movesToFailedAndSavesHistoryTwice() {
        Request request = new Request("Titulo", "force-fail en la descripcion");
        setId(request, 3L);
        setStatus(request, RequestStatus.APPROVED);

        when(requestRepository.findById(3L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(RequestExecutionMessage.of(3L, "REQ-3", "iago"));

        assertEquals(RequestStatus.FAILED, request.getStatus());
        verify(requestRepository, times(2)).save(request);

        ArgumentCaptor<RequestHistory> captor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(historyRepository, times(2)).save(captor.capture());

        assertEquals(RequestStatus.APPROVED, captor.getAllValues().get(0).getFromStatus());
        assertEquals(RequestStatus.IN_PROGRESS, captor.getAllValues().get(0).getToStatus());
        assertEquals(RequestStatus.IN_PROGRESS, captor.getAllValues().get(1).getFromStatus());
        assertEquals(RequestStatus.FAILED, captor.getAllValues().get(1).getToStatus());
    }
}