package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationMessage;
import com.opsflow.opsflow_backend.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ evita que Spring Security te devuelva 401 en estos tests
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

    // ✅ clave: evita que el contexto intente construir el filtro real (y pida JwtService)
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void create_shouldSetValidatedAndSendRabbitMessage() throws Exception {
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            var f = Request.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(r, 10L);
            return r;
        });

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Titulo OK\",\"description\":\"Descripcion OK\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("VALIDATED"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REQUEST_VALIDATION_QUEUE),
                any(RequestValidationMessage.class)
        );
        verify(historyRepository).save(any(RequestHistory.class));
    }

    @Test
    void approve_shouldWorkOnlyFromPending() throws Exception {
        Request r = new Request("t", "d");
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
    void reject_shouldWorkOnlyFromPending() throws Exception {
        Request r = new Request("t", "d");
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
    void approve_whenRequestNotFound_shouldReturn404() throws Exception {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        mvc.perform(post("/requests/1/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_whenNotPending_shouldReturn400() throws Exception {
        Request r = new Request("t", "desc ok");
        r.submit(); // VALIDATED, no PENDING

        when(requestRepository.findById(2L)).thenReturn(Optional.of(r));

        mvc.perform(post("/requests/2/approve"))
                .andExpect(status().isBadRequest());
    }
}