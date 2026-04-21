package com.opsflow.opsflow_backend.api.dashboard;

import com.opsflow.opsflow_backend.application.dashboard.DashboardService;
import com.opsflow.opsflow_backend.security.jwt.JwtAuthenticationFilter;
import com.opsflow.opsflow_backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DashboardService dashboardService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CacheManager cacheManager;

    @Test
    void summary_shouldReturn200() throws Exception {
        when(dashboardService.getSummary()).thenReturn(new DashboardSummaryDto(
                20L, 2L, 3L, 4L, 1L, 5L, 3L, 1L, 1L
        ));

        mvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(20))
                .andExpect(jsonPath("$.draft").value(2))
                .andExpect(jsonPath("$.pending").value(3))
                .andExpect(jsonPath("$.approved").value(4))
                .andExpect(jsonPath("$.rejected").value(1))
                .andExpect(jsonPath("$.inProgress").value(5))
                .andExpect(jsonPath("$.completed").value(3))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.cancelled").value(1));
    }
}