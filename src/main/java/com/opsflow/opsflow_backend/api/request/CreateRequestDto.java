package com.opsflow.opsflow_backend.api.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRequestDto(

        @NotBlank
        String title,

        String description
) {}