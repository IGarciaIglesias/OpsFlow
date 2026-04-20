package com.opsflow.opsflow_backend.api.user;

import com.opsflow.opsflow_backend.domain.user.AppUser;

public record MeResponseDto(
        Long id,
        String username,
        String role,
        boolean active
) {
    public static MeResponseDto from(AppUser user) {
        return new MeResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isActive()
        );
    }
}