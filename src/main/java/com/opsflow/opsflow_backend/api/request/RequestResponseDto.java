package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;

import java.time.Instant;

public record RequestResponseDto(
        Long id,
        String title,
        String description,
        RequestStatus status,
        Instant createdAt
) {

    public static RequestResponseDto from(Request request) {
        return new RequestResponseDto(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}