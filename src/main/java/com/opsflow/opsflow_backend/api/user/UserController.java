package com.opsflow.opsflow_backend.api.user;

import com.opsflow.opsflow_backend.infrastructure.persistence.user.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> me(Authentication authentication) {
        String username = authentication.getName();

        return appUserRepository.findByUsername(username)
                .map(MeResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}