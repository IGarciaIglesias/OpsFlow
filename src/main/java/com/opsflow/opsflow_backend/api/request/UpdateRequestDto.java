package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.RequestPriority;
import com.opsflow.opsflow_backend.domain.request.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestDto(

        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotBlank(message = "Creator is required")
        @Size(max = 120, message = "Creator must be at most 120 characters")
        String creator,

        @Size(max = 120, message = "Assignee must be at most 120 characters")
        String assignee,

        @NotNull(message = "Priority is required")
        RequestPriority priority,

        @NotNull(message = "Type is required")
        RequestType type
) {}