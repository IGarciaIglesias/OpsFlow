package com.opsflow.opsflow_backend.security.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTest {

    @Test
    void asAuthority_shouldPrefixRoleName() {
        assertEquals("ROLE_ADMIN", Role.ADMIN.asAuthority());
        assertEquals("ROLE_MANAGER", Role.MANAGER.asAuthority());
        assertEquals("ROLE_OPERATOR", Role.OPERATOR.asAuthority());
        assertEquals("ROLE_VIEWER", Role.VIEWER.asAuthority());
    }
}