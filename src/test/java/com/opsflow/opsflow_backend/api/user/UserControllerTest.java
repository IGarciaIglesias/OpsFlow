package com.opsflow.opsflow_backend.api.user;

import com.opsflow.opsflow_backend.domain.user.AppUser;
import com.opsflow.opsflow_backend.infrastructure.persistence.user.AppUserRepository;
import com.opsflow.opsflow_backend.security.jwt.JwtAuthenticationFilter;
import com.opsflow.opsflow_backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CacheManager cacheManager;

    @Test
    void me_shouldReturn200_whenUserExists() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("iago");

        when(appUserRepository.findByUsername("iago")).thenReturn(Optional.of(user));

        Authentication auth = new UsernamePasswordAuthenticationToken("iago", "pwd");

        mvc.perform(get("/users/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("iago"));
    }

    @Test
    void me_shouldReturn404_whenUserDoesNotExist() throws Exception {
        when(appUserRepository.findByUsername("iago")).thenReturn(Optional.empty());

        Authentication auth = new UsernamePasswordAuthenticationToken("iago", "pwd");

        mvc.perform(get("/users/me").principal(auth))
                .andExpect(status().isNotFound());
    }
}