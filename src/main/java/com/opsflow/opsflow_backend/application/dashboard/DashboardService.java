package com.opsflow.opsflow_backend.application.dashboard;

import com.opsflow.opsflow_backend.api.dashboard.DashboardSummaryDto;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final RequestRepository requestRepository;

    public DashboardService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Cacheable("dashboardSummary")
    public DashboardSummaryDto getSummary() {
        return new DashboardSummaryDto(
                requestRepository.count(),
                requestRepository.countByStatus(RequestStatus.DRAFT),
                requestRepository.countByStatus(RequestStatus.PENDING),
                requestRepository.countByStatus(RequestStatus.APPROVED),
                requestRepository.countByStatus(RequestStatus.REJECTED),
                requestRepository.countByStatus(RequestStatus.IN_PROGRESS),
                requestRepository.countByStatus(RequestStatus.COMPLETED),
                requestRepository.countByStatus(RequestStatus.FAILED),
                requestRepository.countByStatus(RequestStatus.CANCELLED)
        );
    }
}