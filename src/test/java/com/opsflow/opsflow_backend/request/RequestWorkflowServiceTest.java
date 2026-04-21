package com.opsflow.opsflow_backend.request;

import com.opsflow.opsflow_backend.application.request.RequestWorkflowService;
import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.execution.RequestExecutionProducer;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationProducer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestWorkflowServiceTest {

    private RequestRepository requestRepository;
    private RequestHistoryRepository historyRepository;
    private RequestValidationProducer validationProducer;
    private RequestExecutionProducer executionProducer;
    private RequestWorkflowService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        historyRepository = mock(RequestHistoryRepository.class);
        validationProducer = mock(RequestValidationProducer.class);
        executionProducer = mock(RequestExecutionProducer.class);
        service = new RequestWorkflowService(
                requestRepository,
                historyRepository,
                validationProducer,
                executionProducer
        );
    }

    private static void setId(Request request, long id) {
        try {
            Field field = Request.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(request, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setStatus(Request request, RequestStatus status) {
        try {
            Field field = Request.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(request, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void submit_shouldMoveDraftToPendingAndSendValidation() {
        Request request = new Request("Titulo", "Descripcion suficiente para validar");
        setId(request, 1L);
        setStatus(request, RequestStatus.DRAFT);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = service.submit(1L, "iago");

        assertEquals(RequestStatus.PENDING, result.getStatus());
        verify(requestRepository).save(request);
        verify(historyRepository).save(any(RequestHistory.class));
        verify(validationProducer).send(1L, request.getCode(), "iago");
        verifyNoInteractions(executionProducer);
    }

    @Test
    void submit_shouldThrowWhenNotFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.submit(1L, "iago"));

        verifyNoInteractions(historyRepository, validationProducer, executionProducer);
    }

    @Test
    void submit_shouldThrowWhenNotDraft() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 1L);
        setStatus(request, RequestStatus.PENDING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.submit(1L, "iago")
        );

        assertEquals("Only DRAFT requests can be submitted", ex.getMessage());
        verify(requestRepository, never()).save(any());
        verifyNoInteractions(historyRepository, validationProducer, executionProducer);
    }

    @Test
    void approve_shouldMoveValidatedToApprovedAndSendExecution() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 2L);
        setStatus(request, RequestStatus.VALIDATED);

        when(requestRepository.findById(2L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = service.approve(2L, "iago");

        assertEquals(RequestStatus.APPROVED, result.getStatus());
        verify(requestRepository).save(request);
        verify(historyRepository).save(any(RequestHistory.class));
        verify(executionProducer).send(2L, request.getCode(), "iago");
        verifyNoInteractions(validationProducer);
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        when(requestRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.approve(2L, "iago"));

        verifyNoInteractions(historyRepository, validationProducer, executionProducer);
    }

    @Test
    void approve_shouldThrowWhenNotValidated() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 2L);
        setStatus(request, RequestStatus.PENDING);

        when(requestRepository.findById(2L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approve(2L, "iago")
        );

        assertEquals("Only VALIDATED requests can be approved", ex.getMessage());
        verify(requestRepository, never()).save(any());
        verifyNoInteractions(historyRepository, validationProducer, executionProducer);
    }

    @Test
    void reject_shouldMoveAnyStatusToRejected() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 3L);
        setStatus(request, RequestStatus.PENDING);

        when(requestRepository.findById(3L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = service.reject(3L);

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(requestRepository).save(request);
        verify(historyRepository).save(any(RequestHistory.class));
        verifyNoInteractions(validationProducer, executionProducer);
    }

    @Test
    void cancel_shouldMoveAnyStatusToCancelled() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 4L);
        setStatus(request, RequestStatus.PENDING);

        when(requestRepository.findById(4L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = service.cancel(4L);

        assertEquals(RequestStatus.CANCELLED, result.getStatus());
        verify(requestRepository).save(request);
        verify(historyRepository).save(any(RequestHistory.class));
        verifyNoInteractions(validationProducer, executionProducer);
    }

    @Test
    void retry_shouldMoveFailedToDraft() {
        Request request = new Request("Titulo", "Descripcion suficiente");
        setId(request, 5L);
        setStatus(request, RequestStatus.FAILED);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request result = service.retry(5L);

        assertEquals(RequestStatus.DRAFT, result.getStatus());
        verify(requestRepository).save(request);
        verify(historyRepository).save(any(RequestHistory.class));
        verifyNoInteractions(validationProducer, executionProducer);
    }
}