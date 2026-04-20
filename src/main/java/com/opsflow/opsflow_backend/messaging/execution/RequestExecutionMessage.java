package com.opsflow.opsflow_backend.messaging.execution;

public record RequestExecutionMessage(Long requestId) {
    public static RequestExecutionMessage of(Long requestId) {
        return new RequestExecutionMessage(requestId);
    }
}