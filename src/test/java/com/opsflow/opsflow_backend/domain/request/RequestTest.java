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

    @Test
    void constructor_setsDraftAndCreatedAt() {
        Request r = new Request("t", "desc");
        assertEquals(RequestStatus.DRAFT, r.getStatus());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void validate_movesValidatedToPending() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.VALIDATED);

        r.validate();

        assertEquals(RequestStatus.PENDING, r.getStatus());
    }

    @Test
    void validate_throwsIfNotValidated() {
        Request r = new Request("title", "desc");

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::validate);
        assertTrue(ex.getMessage().contains("Only VALIDATED"));
    }

    @Test
    void validationFailed_movesValidatedToRejected() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.VALIDATED);

        r.validationFailed();

        assertEquals(RequestStatus.REJECTED, r.getStatus());
    }

    @Test
    void validationFailed_throwsIfNotValidated() {
        Request r = new Request("title", "desc");

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::validationFailed);
        assertTrue(ex.getMessage().contains("Only VALIDATED"));
    }

    @Test
    void approve_movesPendingToApproved() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.approve();

        assertEquals(RequestStatus.APPROVED, r.getStatus());
    }

    @Test
    void reject_movesPendingToRejected() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.reject();

        assertEquals(RequestStatus.REJECTED, r.getStatus());
    }

    @Test
    void approve_throwsIfNotPending() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.VALIDATED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::approve);
        assertTrue(ex.getMessage().contains("Only PENDING"));
    }

    @Test
    void reject_throwsIfNotPending() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.VALIDATED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::reject);
        assertTrue(ex.getMessage().contains("Only PENDING"));
    }

    @Test
    void retry_movesRejectedToDraft() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.REJECTED);

        r.retry();

        assertEquals(RequestStatus.DRAFT, r.getStatus());
    }

    @Test
    void retry_throwsIfNotRejected() {
        Request r = new Request("title", "desc");

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::retry);
        assertTrue(ex.getMessage().contains("Only REJECTED"));
    }
}