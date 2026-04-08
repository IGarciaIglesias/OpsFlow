package com.opsflow.opsflow_backend.infrastructure.persistence.request;

import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestHistoryRepository
        extends JpaRepository<RequestHistory, Long> {

    List<RequestHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);
}
