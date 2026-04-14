package com.opsflow.opsflow_backend.domain.request;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Request() {}

    public Request(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = RequestStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    // =====================
    // Lógica de dominio
    // =====================

    /** DRAFT -> VALIDATED (se lanza al crear o al enviar) */
    public void submit() {
        if (status != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted");
        }
        this.status = RequestStatus.VALIDATED;
    }

    /** VALIDATED -> PENDING (pasa a revisión humana) */
    public void validate() {
        if (status != RequestStatus.VALIDATED) {
            throw new IllegalStateException("Only VALIDATED requests can be validated");
        }
        this.status = RequestStatus.PENDING;
    }

    /** VALIDATED -> REJECTED (rechazo automático por validación técnica) */
    public void validationFailed() {
        if (status != RequestStatus.VALIDATED) {
            throw new IllegalStateException("Only VALIDATED requests can fail validation");
        }
        this.status = RequestStatus.REJECTED;
    }

    /** PENDING -> APPROVED */
    public void approve() {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }
        this.status = RequestStatus.APPROVED;
    }

    /** PENDING -> REJECTED (rechazo humano) */
    public void reject() {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }
        this.status = RequestStatus.REJECTED;
    }

    /** REJECTED -> DRAFT (reintento manual) */
    public void retry() {
        if (status != RequestStatus.REJECTED) {
            throw new IllegalStateException("Only REJECTED requests can be retried");
        }
        this.status = RequestStatus.DRAFT;
    }

    // =====================
    // Getters
    // =====================

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public RequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}