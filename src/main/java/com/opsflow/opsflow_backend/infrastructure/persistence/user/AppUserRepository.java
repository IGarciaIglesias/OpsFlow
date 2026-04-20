package com.opsflow.opsflow_backend.infrastructure.persistence.user;

import com.opsflow.opsflow_backend.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}