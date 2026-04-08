package com.opsflow.opsflow_backend.infrastructure.persistence.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
}