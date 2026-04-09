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


    public void submit() {
        if (status != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted");
        }
        this.status = RequestStatus.PENDING;
    }

    public void validate() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be validated");
        }
        this.status = RequestStatus.VALIDATED;
    }

    public void approve() {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }
        this.status = RequestStatus.APPROVED;
    }

    public void reject() {
        if (this.status != RequestStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Only VALIDATED requests can be rejected"
            );
        }
        this.status = RequestStatus.REJECTED;
    }

    // =====================
    // Getters
    // =====================

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void cancel() {
        if (status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) {
            throw new IllegalStateException(
                    "Cannot cancel a request that is already finished"
            );
        }
        this.status = RequestStatus.CANCELLED;
    }

    public void retry() {
        if (status != RequestStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only REJECTED requests can be retried"
            );
        }
        this.status = RequestStatus.PENDING;
    }
}