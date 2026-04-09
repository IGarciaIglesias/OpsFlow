package com.opsflow.opsflow_backend.messaging.validation;

import java.time.Instant;
import java.util.UUID;

public record RequestValidationMessage(
        UUID messageId,
        Long requestId,
        Instant timestamp
) {
    public static RequestValidationMessage of(Long requestId) {
        return new RequestValidationMessage(
                UUID.randomUUID(),
                requestId,
                Instant.now()
        );
    }
}