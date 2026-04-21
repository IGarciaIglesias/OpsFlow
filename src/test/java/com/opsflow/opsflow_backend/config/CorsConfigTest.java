package com.opsflow.opsflow_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void corsFilter_shouldBeConfiguredCorrectly() throws Exception {
        CorsConfig config = new CorsConfig();
        CorsFilter filter = config.corsFilter();

        assertNotNull(filter);

        Field sourceField = CorsFilter.class.getDeclaredField("configSource");
        sourceField.setAccessible(true);
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) sourceField.get(filter);

        Field configMapField = UrlBasedCorsConfigurationSource.class.getDeclaredField("corsConfigurations");
        configMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, CorsConfiguration> configs = (Map<String, CorsConfiguration>) configMapField.get(source);

        CorsConfiguration cors = configs.get("/**");
        assertNotNull(cors);
        assertEquals("http://localhost:4200", cors.getAllowedOrigins().get(0));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedMethods().contains("PUT"));
        assertTrue(cors.getAllowedMethods().contains("DELETE"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(cors.getAllowCredentials());
    }
}