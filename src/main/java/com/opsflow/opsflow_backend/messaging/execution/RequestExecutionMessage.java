package com.opsflow.opsflow_backend.messaging.execution;

import java.time.Instant;
import java.util.UUID;

public record RequestExecutionMessage(
        UUID messageId,
        String correlationId,
        Long requestId,
        String requestCode,
        String eventType,
        String requestedBy,
        Instant timestamp
) {
    public static RequestExecutionMessage of(
            Long requestId,
            String requestCode,
            String requestedBy
    ) {
        String correlationId = UUID.randomUUID().toString();

        return new RequestExecutionMessage(
                UUID.randomUUID(),
                correlationId,
                requestId,
                requestCode,
                "REQUEST_APPROVED",
                requestedBy,
                Instant.now()
        );
    }
}