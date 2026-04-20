package com.opsflow.opsflow_backend.api.auth;

import com.opsflow.opsflow_backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private MockMvc mvc;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);

        AuthController controller = new AuthController(authenticationManager, jwtService);
        mvc = standaloneSetup(controller).build();
    }

    @Test
    void login_ok_shouldReturnToken() throws Exception {
        UserDetails admin = User.withUsername("ADMIN")
                .password("ADMIN")
                .authorities("ROLE_ADMIN")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(auth);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("TOKEN123");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ADMIN",
                                  "password": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TOKEN123"));
    }
}