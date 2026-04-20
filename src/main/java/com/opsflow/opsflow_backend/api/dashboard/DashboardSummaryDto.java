package com.opsflow.opsflow_backend.api.dashboard;

public record DashboardSummaryDto(
        long total,
        long draft,
        long pending,
        long approved,
        long rejected,
        long inProgress,
        long completed,
        long failed,
        long cancelled
) {}