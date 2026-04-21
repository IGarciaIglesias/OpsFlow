package com.opsflow.opsflow_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void corsFilter_shouldBeConfiguredCorrectly() throws Exception {
        CorsConfig config = new CorsConfig();
        CorsFilter filter = config.corsFilter();

        assertNotNull(filter);

        Field field = CorsFilter.class.getDeclaredField("configSource");
        field.setAccessible(true);
        CorsConfigurationSource source = (CorsConfigurationSource) field.get(filter);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/anything");
        request.addHeader("Origin", "http://localhost:4200");

        var cors = source.getCorsConfiguration(request);

        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedMethods().contains("PUT"));
        assertTrue(cors.getAllowedMethods().contains("DELETE"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(Boolean.TRUE.equals(cors.getAllowCredentials()));
    }
}