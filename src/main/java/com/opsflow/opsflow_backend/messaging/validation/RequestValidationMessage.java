package com.opsflow.opsflow_backend.messaging.validation;

import java.time.Instant;
import java.util.UUID;

public record RequestValidationMessage(
        UUID messageId,
        String correlationId,
        Long requestId,
        String requestCode,
        String eventType,
        String requestedBy,
        Instant timestamp
) {
    public static RequestValidationMessage of(
            Long requestId,
            String requestCode,
            String requestedBy
    ) {
        String correlationId = UUID.randomUUID().toString();

        return new RequestValidationMessage(
                UUID.randomUUID(),
                correlationId,
                requestId,
                requestCode,
                "REQUEST_SUBMITTED",
                requestedBy,
                Instant.now()
        );
    }
}