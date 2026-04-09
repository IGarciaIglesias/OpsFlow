package com.opsflow.opsflow_backend.security.model;

public enum Role {

    ADMIN,
    MANAGER,
    OPERATOR,
    VIEWER;

    /**
     * Conveniencia para Spring Security:
     * convierte ADMIN → ROLE_ADMIN
     */
    public String asAuthority() {
        return "ROLE_" + this.name();
    }
}