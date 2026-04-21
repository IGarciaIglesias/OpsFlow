package com.opsflow.opsflow_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_shouldEncode() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String encoded = encoder.encode("secret");

        assertNotEquals("secret", encoded);
        assertTrue(encoder.matches("secret", encoded));
    }
}