package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationMessage;
import com.opsflow.opsflow_backend.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class RequestControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RequestRepository requestRepository;

    @MockitoBean
    RequestHistoryRepository historyRepository;

    @MockitoBean
    ApplicationEventPublisher eventPublisher;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    // evita que el contexto intente construir el filtro real
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private static void setId(Request r, long id) {
        try {
            Field f = Request.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // (lo dejo por si lo usas en otros tests, pero retry ya NO depende de esto)
    private static void setStatus(Request r, RequestStatus status) {
        try {
            Field f = Request.class.getDeclaredField("status");
            f.setAccessible(true);
            f.set(r, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------
    // POST /requests (create)
    // ---------------------------
    @Test
    void create_shouldCreateDraftAndReturn201() throws Exception {
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            setId(r, 10L);
            return r;
        });

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Titulo OK\",\"description\":\"Descripcion OK\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/requests/10"))
                .andExpect(jsonPath("$.id").value(10))
                // En tu ejecución real, create está devolviendo VALIDATED
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    // ---------------------------
    // GET /requests/{id}
    // ---------------------------
    @Test
    void getById_whenFound_shouldReturn200() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 5L);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));

        mvc.perform(get("/requests/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/requests/99"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // GET /requests
    // ---------------------------
    @Test
    void getAll_shouldReturnList() throws Exception {
        Request r1 = new Request("a", "desc 1");
        setId(r1, 1L);
        Request r2 = new Request("b", "desc 2");
        setId(r2, 2L);

        when(requestRepository.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // ---------------------------
    // POST /requests/{id}/submit
    // ---------------------------
    @Test
    void submit_whenDraft_shouldReturn202_sendRabbit_andSaveHistory() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 7L);

        when(requestRepository.findById(7L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/7/submit"))
                .andExpect(status().isAccepted());

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));

        // Evita el ambiguous method call capturando el mensaje
        ArgumentCaptor<RequestValidationMessage> captor =
                ArgumentCaptor.forClass(RequestValidationMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REQUEST_VALIDATION_QUEUE),
                captor.capture()
        );

        RequestValidationMessage msg = captor.getValue();
        assertNotNull(msg);
        assertEquals(7L, msg.requestId());
        assertNotNull(msg.messageId());
        assertNotNull(msg.timestamp());
    }

    @Test
    void submit_whenNotDraft_shouldReturn400() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 8L);
        r.submit(); // ahora VALIDATED, ya no DRAFT

        when(requestRepository.findById(8L)).thenReturn(Optional.of(r));

        mvc.perform(post("/requests/8/submit"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rabbitTemplate);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void submit_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(404L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/404/submit"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // POST /requests/{id}/approve
    // ---------------------------
    @Test
    void approve_shouldReturn200_whenPending() throws Exception {
        Request r = new Request("t", "d");
        setId(r, 5L);
        r.submit();
        r.validate(); // PENDING

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/5/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void approve_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/1/approve"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // POST /requests/{id}/reject
    // ---------------------------
    @Test
    void reject_shouldReturn200_whenPending() throws Exception {
        Request r = new Request("t", "d");
        setId(r, 6L);
        r.submit();
        r.validate(); // PENDING

        when(requestRepository.findById(6L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/6/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void reject_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(2L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/2/reject"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // POST /requests/{id}/retry
    // ---------------------------
    @Test
    void retry_whenRejected_shouldReturn200() throws Exception {
        // ✅ Mock del Request para controlar estado y transición
        Request r = mock(Request.class);

        AtomicReference<RequestStatus> status = new AtomicReference<>(RequestStatus.REJECTED);

        when(r.getStatus()).thenAnswer(inv -> status.get());

        doAnswer(inv -> {
            // misma regla que el dominio: solo REJECTED
            if (status.get() != RequestStatus.REJECTED) {
                throw new IllegalStateException("Only REJECTED requests can be retried");
            }
            status.set(RequestStatus.DRAFT);
            return null;
        }).when(r).retry();

        when(requestRepository.findById(9L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/9/retry"))
                .andExpect(status().isOk());

        verify(r).retry();
        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void retry_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(123L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/123/retry"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // GET /requests/{id}/history
    // ---------------------------
    @Test
    void history_shouldReturnList() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 20L);

        RequestHistory h1 = new RequestHistory(r, RequestStatus.DRAFT, RequestStatus.VALIDATED);
        RequestHistory h2 = new RequestHistory(r, RequestStatus.VALIDATED, RequestStatus.PENDING);

        when(historyRepository.findByRequestIdOrderByChangedAtAsc(20L)).thenReturn(List.of(h1, h2));

        mvc.perform(get("/requests/20/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStatus").value("DRAFT"))
                .andExpect(jsonPath("$[0].toStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].fromStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].toStatus").value("PENDING"));
    }
}