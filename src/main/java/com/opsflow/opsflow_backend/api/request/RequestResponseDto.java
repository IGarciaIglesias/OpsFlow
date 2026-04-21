package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestPriority;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.RequestType;

import java.time.Instant;

public record RequestResponseDto(
        Long id,
        String code,
        String title,
        String description,
        String creator,
        String assignee,
        RequestPriority priority,
        RequestType type,
        RequestStatus status,
        Instant createdAt
) {

    public static RequestResponseDto from(Request request) {
        return new RequestResponseDto(
                request.getId(),
                request.getCode(),
                request.getTitle(),
                request.getDescription(),
                request.getCreator(),
                request.getAssignee(),
                request.getPriority(),
                request.getType(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    public record RequestHistoryDto(
            String fromStatus,
            String toStatus,
            Instant changedAt
    ) {
        public static RequestHistoryDto from(RequestHistory h) {
            return new RequestHistoryDto(
                    h.getFromStatus().name(),
                    h.getToStatus().name(),
                    h.getChangedAt()
            );
        }
    }
}