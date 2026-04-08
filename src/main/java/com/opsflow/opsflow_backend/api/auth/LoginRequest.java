package com.opsflow.opsflow_backend.api.auth;

public record LoginRequest(
        String username,
        String password
) {}