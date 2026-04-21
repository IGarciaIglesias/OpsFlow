package com.opsflow.opsflow_backend.application.dashboard;

import com.opsflow.opsflow_backend.api.dashboard.DashboardSummaryDto;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    private RequestRepository requestRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        dashboardService = new DashboardService(requestRepository);
    }

    @Test
    void getSummary_shouldReturnAllCounters() {
        when(requestRepository.count()).thenReturn(20L);
        when(requestRepository.countByStatus(RequestStatus.DRAFT)).thenReturn(2L);
        when(requestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(3L);
        when(requestRepository.countByStatus(RequestStatus.APPROVED)).thenReturn(4L);
        when(requestRepository.countByStatus(RequestStatus.REJECTED)).thenReturn(1L);
        when(requestRepository.countByStatus(RequestStatus.IN_PROGRESS)).thenReturn(5L);
        when(requestRepository.countByStatus(RequestStatus.COMPLETED)).thenReturn(3L);
        when(requestRepository.countByStatus(RequestStatus.FAILED)).thenReturn(1L);
        when(requestRepository.countByStatus(RequestStatus.CANCELLED)).thenReturn(1L);

        DashboardSummaryDto result = dashboardService.getSummary();

        assertEquals(20L, result.total());
        assertEquals(2L, result.draft());
        assertEquals(3L, result.pending());
        assertEquals(4L, result.approved());
        assertEquals(1L, result.rejected());
        assertEquals(5L, result.inProgress());
        assertEquals(3L, result.completed());
        assertEquals(1L, result.failed());
        assertEquals(1L, result.cancelled());
    }
}