package com.opsflow.opsflow_backend.infrastructure.persistence.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findByStatus(RequestStatus status, Pageable pageable);

    long countByStatus(RequestStatus status);
}