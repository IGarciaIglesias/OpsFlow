package com.opsflow.opsflow_backend.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_extractUsername_and_extractRole_shouldWork() {
        UserDetails admin = User.withUsername("ADMIN")
                .password("ADMIN")
                .authorities("ROLE_ADMIN")
                .build();

        String token = jwtService.generateToken(admin);
        assertNotNull(token);

        assertEquals("ADMIN", jwtService.extractUsername(token));
        assertEquals("ROLE_ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        UserDetails viewer = User.withUsername("VIEWER")
                .password("VIEWER")
                .authorities("ROLE_VIEWER")
                .build();

        String token = jwtService.generateToken(viewer);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forInvalidToken() {
        assertFalse(jwtService.isTokenValid("no-es-un-jwt"));
    }
}