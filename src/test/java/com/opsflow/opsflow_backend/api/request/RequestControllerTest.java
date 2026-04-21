package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.event.RequestApprovedEvent;
import com.opsflow.opsflow_backend.domain.request.event.RequestRejectedEvent;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import com.opsflow.opsflow_backend.messaging.execution.RequestExecutionMessage;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationMessage;
import com.opsflow.opsflow_backend.security.CustomUserDetailsService;
import com.opsflow.opsflow_backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

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

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

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
    void create_shouldCreateDraftAndReturn201() throws Exception {
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            setId(r, 10L);
            return r;
        });

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titulo OK",
                                  "description": "Descripcion OK",
                                  "creator": "Iago",
                                  "assignee": "Ana",
                                  "priority": "HIGH",
                                  "type": "INCIDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/requests/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Titulo OK"))
                .andExpect(jsonPath("$.description").value("Descripcion OK"))
                .andExpect(jsonPath("$.creator").value("Iago"))
                .andExpect(jsonPath("$.assignee").value("Ana"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.type").value("INCIDENT"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void create_whenInvalid_shouldReturn400() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Descripcion OK",
                                  "creator": "",
                                  "assignee": "Ana",
                                  "priority": null,
                                  "type": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getById_whenFound_shouldReturn200() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 5L);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));

        mvc.perform(get("/requests/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("t"))
                .andExpect(jsonPath("$.description").value("desc ok"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/requests/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPaginatedList() throws Exception {
        Request r1 = new Request("a", "desc 1");
        setId(r1, 1L);

        Request r2 = new Request("b", "desc 2");
        setId(r2, 2L);

        Page<Request> page = new PageImpl<>(List.of(r1, r2));

        when(requestRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("a"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("b"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAll_whenStatusProvided_shouldUseFilteredRepositoryMethod() throws Exception {
        Request r1 = new Request("a", "desc 1");
        setId(r1, 1L);

        Page<Request> page = new PageImpl<>(List.of(r1));

        when(requestRepository.findByStatus(eq(RequestStatus.APPROVED), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/requests")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(requestRepository).findByStatus(eq(RequestStatus.APPROVED), any(org.springframework.data.domain.Pageable.class));
        verify(requestRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void update_shouldReturn200_whenDraft() throws Exception {
        Request r = new Request("old", "old desc");
        setId(r, 3L);

        when(requestRepository.findById(3L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(put("/requests/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "new title",
                                  "description": "new description",
                                  "creator": "creator",
                                  "assignee": "assignee",
                                  "priority": "LOW",
                                  "type": "SUPPORT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new title"))
                .andExpect(jsonPath("$.description").value("new description"))
                .andExpect(jsonPath("$.creator").value("creator"))
                .andExpect(jsonPath("$.assignee").value("assignee"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.type").value("SUPPORT"));
    }

    @Test
    void update_whenInvalid_shouldReturn400() throws Exception {
        Request r = new Request("old", "old desc");
        setId(r, 3L);

        when(requestRepository.findById(3L)).thenReturn(Optional.of(r));

        mvc.perform(put("/requests/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "",
                                  "creator": "",
                                  "assignee": "assignee",
                                  "priority": null,
                                  "type": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(requestRepository, never()).save(any());
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(3L)).thenReturn(Optional.empty());

        mvc.perform(put("/requests/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "new title",
                                  "description": "new description",
                                  "creator": "creator",
                                  "assignee": "assignee",
                                  "priority": "LOW",
                                  "type": "SUPPORT"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void submit_whenDraft_shouldReturn202_sendRabbit_andSaveHistory() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 7L);

        when(requestRepository.findById(7L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/7/submit"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));

        ArgumentCaptor<RequestValidationMessage> captor = ArgumentCaptor.forClass(RequestValidationMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REQUEST_VALIDATION_QUEUE),
                captor.capture()
        );

        RequestValidationMessage msg = captor.getValue();
        assertNotNull(msg);
        assertEquals(7L, msg.requestId());
        assertNotNull(msg.messageId());
        assertNotNull(msg.correlationId());
    }

    @Test
    void submit_whenNotDraft_shouldReturn400() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 8L);
        setStatus(r, RequestStatus.VALIDATED);

        when(requestRepository.findById(8L)).thenReturn(Optional.of(r));

        mvc.perform(post("/requests/8/submit"))
                .andExpect(status().isBadRequest());

        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void submit_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(404L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/404/submit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturn200_whenValidated() throws Exception {
        Request r = new Request("t", "descripcion suficiente");
        setId(r, 5L);
        setStatus(r, RequestStatus.VALIDATED);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/5/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
        verify(eventPublisher).publishEvent(any(RequestApprovedEvent.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.REQUEST_EXECUTION_QUEUE), any(RequestExecutionMessage.class));
    }

    @Test
    void approve_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(5L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/5/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reject_shouldReturn200_whenPending() throws Exception {
        Request r = new Request("t", "descripcion suficiente");
        setId(r, 6L);
        setStatus(r, RequestStatus.PENDING);

        when(requestRepository.findById(6L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/6/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
        verify(eventPublisher).publishEvent(any(RequestRejectedEvent.class));
    }

    @Test
    void reject_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(6L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/6/reject"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancel_shouldReturn200_whenAllowed() throws Exception {
        Request r = new Request("t", "descripcion suficiente");
        setId(r, 11L);
        setStatus(r, RequestStatus.APPROVED);

        when(requestRepository.findById(11L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/11/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void cancel_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(11L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/11/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    void retry_shouldReturn200_whenRejected() throws Exception {
        Request r = new Request("t", "descripcion suficiente");
        setId(r, 9L);
        setStatus(r, RequestStatus.REJECTED);

        when(requestRepository.findById(9L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/requests/9/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(requestRepository).save(r);
        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void retry_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(123L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/123/retry"))
                .andExpect(status().isNotFound());
    }

    @Test
    void history_shouldReturnList() throws Exception {
        Request r = new Request("t", "desc ok");
        setId(r, 20L);

        RequestHistory h1 = new RequestHistory(r, RequestStatus.DRAFT, RequestStatus.VALIDATED);
        RequestHistory h2 = new RequestHistory(r, RequestStatus.VALIDATED, RequestStatus.PENDING);

        when(requestRepository.existsById(20L)).thenReturn(true);
        when(historyRepository.findByRequestIdOrderByChangedAtAsc(20L)).thenReturn(List.of(h1, h2));

        mvc.perform(get("/requests/20/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStatus").value("DRAFT"))
                .andExpect(jsonPath("$[0].toStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].fromStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].toStatus").value("PENDING"));
    }

    @Test
    void history_whenNotFound_shouldReturn404() throws Exception {
        when(requestRepository.existsById(404L)).thenReturn(false);

        mvc.perform(get("/requests/404/history"))
                .andExpect(status().isNotFound());
    }
}