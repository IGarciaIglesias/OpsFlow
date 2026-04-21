package com.opsflow.opsflow_backend.security;

import com.opsflow.opsflow_backend.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_shouldEncode() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String encoded = encoder.encode("secret");

        assertNotEquals("secret", encoded);
        assertTrue(encoder.matches("secret", encoded));
    }

    @Test
    void authenticationManager_shouldBeResolvableFromContext() {
        new ApplicationContextRunner()
                .withBean(SecurityConfig.class, () -> securityConfig)
                .withBean(JwtAuthenticationFilter.class, () -> mock(JwtAuthenticationFilter.class))
                .run(context -> {
                    assertTrue(context.containsBean("authenticationManager"));
                    AuthenticationManager manager = context.getBean(AuthenticationManager.class);
                    assertNotNull(manager);
                });
    }
}