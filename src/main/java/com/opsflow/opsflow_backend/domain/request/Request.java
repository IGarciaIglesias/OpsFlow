package com.opsflow.opsflow_backend.domain.request;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 120)
    private String creator;

    @Column(length = 120)
    private String assignee;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Request() {}

    public Request(
            String title,
            String description,
            String creator,
            String assignee,
            RequestPriority priority,
            RequestType type
    ) {
        this.code = generateCode();
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.assignee = assignee;
        this.priority = priority;
        this.type = type;
        this.status = RequestStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public Request(String title, String description) {
        this(
                title,
                description,
                null,
                null,
                RequestPriority.MEDIUM,
                RequestType.SUPPORT
        );
    }

    @PrePersist
    void prePersist() {
        if (this.code == null || this.code.isBlank()) {
            this.code = generateCode();
        }
        if (this.status == null) {
            this.status = RequestStatus.DRAFT;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.priority == null) {
            this.priority = RequestPriority.MEDIUM;
        }
        if (this.type == null) {
            this.type = RequestType.SUPPORT;
        }
    }

    private String generateCode() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void submitForValidation() {
        ensureStatus(RequestStatus.DRAFT);
        this.status = RequestStatus.PENDING;
    }

    public void validate() {
        ensureStatus(RequestStatus.PENDING);
        this.status = RequestStatus.VALIDATED;
    }

    public void validationFailed() {
        ensureStatus(RequestStatus.PENDING);
        this.status = RequestStatus.FAILED;
    }

    public void reject() {
        if (this.status != RequestStatus.PENDING && this.status != RequestStatus.VALIDATED) {
            throw new IllegalStateException("Only PENDING or VALIDATED requests can be rejected");
        }
        this.status = RequestStatus.REJECTED;
    }

    public void approve() {
        ensureStatus(RequestStatus.VALIDATED);
        this.status = RequestStatus.APPROVED;
    }

    public void retry() {
        if (this.status != RequestStatus.REJECTED && this.status != RequestStatus.FAILED) {
            throw new IllegalStateException("Only REJECTED or FAILED requests can be retried");
        }
        this.status = RequestStatus.DRAFT;
    }

    public void updateDraft(String title, String description) {
        updateDraft(title, description, this.creator, this.assignee, this.priority, this.type);
    }

    public void updateDraft(
            String title,
            String description,
            String creator,
            String assignee,
            RequestPriority priority,
            RequestType type
    ) {
        ensureStatus(RequestStatus.DRAFT);
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.assignee = assignee;
        this.priority = priority;
        this.type = type;
    }

    public void cancel() {
        if (status != RequestStatus.DRAFT &&
                status != RequestStatus.PENDING &&
                status != RequestStatus.VALIDATED &&
                status != RequestStatus.APPROVED) {
            throw new IllegalStateException("Request cannot be cancelled in status: " + status);
        }
        this.status = RequestStatus.CANCELLED;
    }

    public void startExecution() {
        ensureStatus(RequestStatus.APPROVED);
        this.status = RequestStatus.IN_PROGRESS;
    }

    public void completeExecution() {
        ensureStatus(RequestStatus.IN_PROGRESS);
        this.status = RequestStatus.COMPLETED;
    }

    public void failExecution() {
        ensureStatus(RequestStatus.IN_PROGRESS);
        this.status = RequestStatus.FAILED;
    }

    private void ensureStatus(RequestStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Invalid transition. Expected " + expected + " but was " + this.status
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public String getAssignee() {
        return assignee;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public RequestType getType() {
        return type;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}