package com.opsflow.opsflow_backend.domain.request;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "request_history")
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus toStatus;

    @Column(nullable = false)
    private Instant changedAt;

    protected RequestHistory() {}

    public RequestHistory(
            Request request,
            RequestStatus fromStatus,
            RequestStatus toStatus
    ) {
        this.request = request;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = Instant.now();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Request getRequest() {
        return request;
    }

    public RequestStatus getFromStatus() {
        return fromStatus;
    }

    public RequestStatus getToStatus() {
        return toStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
