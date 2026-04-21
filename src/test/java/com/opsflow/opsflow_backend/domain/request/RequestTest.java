package com.opsflow.opsflow_backend.domain.request;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private static void setStatus(Request r, RequestStatus status) {
        try {
            Field f = Request.class.getDeclaredField("status");
            f.setAccessible(true);
            f.set(r, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setCode(Request r, String code) {
        try {
            Field f = Request.class.getDeclaredField("code");
            f.setAccessible(true);
            f.set(r, code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setCreatedAt(Request r, java.time.Instant createdAt) {
        try {
            Field f = Request.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(r, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPriority(Request r, RequestPriority priority) {
        try {
            Field f = Request.class.getDeclaredField("priority");
            f.setAccessible(true);
            f.set(r, priority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setType(Request r, RequestType type) {
        try {
            Field f = Request.class.getDeclaredField("type");
            f.setAccessible(true);
            f.set(r, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void constructor_setsDefaults() {
        Request r = new Request("t", "desc");
        assertEquals(RequestStatus.DRAFT, r.getStatus());
        assertNotNull(r.getCreatedAt());
        assertNotNull(r.getCode());
        assertEquals(RequestPriority.MEDIUM, r.getPriority());
        assertEquals(RequestType.SUPPORT, r.getType());
    }

    @Test
    void submitForValidation_movesDraftToPending() {
        Request r = new Request("title", "desc");
        r.submitForValidation();
        assertEquals(RequestStatus.PENDING, r.getStatus());
    }

    @Test
    void validate_movesPendingToValidated() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.validate();

        assertEquals(RequestStatus.VALIDATED, r.getStatus());
    }

    @Test
    void validationFailed_movesPendingToFailed() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.validationFailed();

        assertEquals(RequestStatus.FAILED, r.getStatus());
    }

    @Test
    void reject_worksInPendingAndValidated() {
        Request pending = new Request("title", "desc");
        setStatus(pending, RequestStatus.PENDING);
        pending.reject();
        assertEquals(RequestStatus.REJECTED, pending.getStatus());

        Request validated = new Request("title", "desc");
        setStatus(validated, RequestStatus.VALIDATED);
        validated.reject();
        assertEquals(RequestStatus.REJECTED, validated.getStatus());
    }

    @Test
    void approve_worksOnlyWhenValidated() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.VALIDATED);

        r.approve();

        assertEquals(RequestStatus.APPROVED, r.getStatus());
    }

    @Test
    void cancel_worksInApproved() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.APPROVED);

        r.cancel();

        assertEquals(RequestStatus.CANCELLED, r.getStatus());
    }

    @Test
    void retry_worksForRejectedAndFailed() {
        Request rejected = new Request("title", "desc");
        setStatus(rejected, RequestStatus.REJECTED);
        rejected.retry();
        assertEquals(RequestStatus.DRAFT, rejected.getStatus());

        Request failed = new Request("title", "desc");
        setStatus(failed, RequestStatus.FAILED);
        failed.retry();
        assertEquals(RequestStatus.DRAFT, failed.getStatus());
    }

    @Test
    void updateDraft_updatesAllFieldsWhenDraft() {
        Request r = new Request("title", "desc");

        r.updateDraft("new", "new desc", "creator", "assignee", RequestPriority.HIGH, RequestType.INCIDENT);

        assertEquals("new", r.getTitle());
        assertEquals("new desc", r.getDescription());
        assertEquals("creator", r.getCreator());
        assertEquals("assignee", r.getAssignee());
        assertEquals(RequestPriority.HIGH, r.getPriority());
        assertEquals(RequestType.INCIDENT, r.getType());
    }

    @Test
    void startExecution_completeExecution_and_failExecution_coverFlow() {
        Request r1 = new Request("title", "desc");
        setStatus(r1, RequestStatus.APPROVED);
        r1.startExecution();
        assertEquals(RequestStatus.IN_PROGRESS, r1.getStatus());
        r1.completeExecution();
        assertEquals(RequestStatus.COMPLETED, r1.getStatus());

        Request r2 = new Request("title", "desc");
        setStatus(r2, RequestStatus.APPROVED);
        r2.startExecution();
        r2.failExecution();
        assertEquals(RequestStatus.FAILED, r2.getStatus());
    }

    @Test
    void prePersist_setsMissingDefaults() {
        Request r = new Request("title", "desc");
        setCode(r, null);
        setStatus(r, null);
        setCreatedAt(r, null);
        setPriority(r, null);
        setType(r, null);

        r.prePersist();

        assertNotNull(r.getCode());
        assertEquals(RequestStatus.DRAFT, r.getStatus());
        assertNotNull(r.getCreatedAt());
        assertEquals(RequestPriority.MEDIUM, r.getPriority());
        assertEquals(RequestType.SUPPORT, r.getType());
    }
}