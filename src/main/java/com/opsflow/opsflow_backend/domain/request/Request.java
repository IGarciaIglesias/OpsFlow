package com.opsflow.opsflow_backend.domain.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    // Constructor requerido por JPA
    protected Request() {}

    // Constructor de dominio
    public Request(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = RequestStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    // =====================
    // Lógica de dominio
    // =====================

    /** DRAFT -> VALIDATED */
    public void submit() {
        if (status != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted");
        }
        this.status = RequestStatus.VALIDATED;
    }

    /** VALIDATED -> PENDING */
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
        // ✅ antes estaba en DRAFT: eso contradice el flujo de Rabbit
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

    /** REJECTED -> DRAFT */
    public void retry() {
        if (status != RequestStatus.REJECTED) {
            throw new IllegalStateException("Only REJECTED requests can be retried");
        }
        this.status = RequestStatus.DRAFT;
    }

    public void updateDraft(String title, String description) {
        if (status != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be edited");
        }
        this.title = title;
        this.description = description;
    }

    public void cancel() {
        if (status != RequestStatus.DRAFT &&
                status != RequestStatus.PENDING &&
                status != RequestStatus.APPROVED) {
            throw new IllegalStateException("Request cannot be cancelled in status: " + status);
        }
        this.status = RequestStatus.CANCELLED;
    }

    public void startExecution() {
        if (status != RequestStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED requests can start execution");
        }
        this.status = RequestStatus.IN_PROGRESS;
    }

    public void completeExecution() {
        if (status != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS requests can be completed");
        }
        this.status = RequestStatus.COMPLETED;
    }

    public void failExecution() {
        if (status != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS requests can fail");
        }
        this.status = RequestStatus.FAILED;
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