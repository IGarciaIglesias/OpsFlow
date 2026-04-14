package com.opsflow.opsflow_backend.security;

import com.opsflow.opsflow_backend.domain.user.AppUser;
import com.opsflow.opsflow_backend.infrastructure.persistence.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Test
    void loadUserByUsername_whenFound_shouldMapToSpringUser() {
        AppUserRepository repo = mock(AppUserRepository.class);
        CustomUserDetailsService service = new CustomUserDetailsService(repo);

        AppUser u = new AppUser();
        u.setUsername("iago");
        u.setPassword("HASH");
        u.setRole("ADMIN");
        u.setActive(true);

        when(repo.findByUsername("iago")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("iago");

        assertEquals("iago", details.getUsername());
        assertEquals("HASH", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_whenNotFound_shouldThrow() {
        AppUserRepository repo = mock(AppUserRepository.class);
        CustomUserDetailsService service = new CustomUserDetailsService(repo);

        when(repo.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}