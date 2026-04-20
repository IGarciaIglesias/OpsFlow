package com.opsflow.opsflow_backend.domain.request;

public enum RequestStatus {
    DRAFT,
    VALIDATED,
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}