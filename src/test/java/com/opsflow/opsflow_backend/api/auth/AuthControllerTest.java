package com.opsflow.opsflow_backend.api.auth;

import com.opsflow.opsflow_backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AuthenticationManager authenticationManager;

    @MockitoBean
    JwtService jwtService;

    @Test
    void login_ok_shouldReturnToken() throws Exception {
        // Usuario real que existe por rol (ADMIN / ADMIN)
        UserDetails admin = User.withUsername("ADMIN")
                .password("ADMIN")
                .authorities("ROLE_ADMIN")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("TOKEN123");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ADMIN\",\"password\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TOKEN123"));
    }
}