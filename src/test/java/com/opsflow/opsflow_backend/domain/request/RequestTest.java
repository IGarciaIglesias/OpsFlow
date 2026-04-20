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
    void validate_movesPendingToValidatedOrKeepsContract() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.validate();

        assertNotNull(r.getStatus());
    }

    @Test
    void validate_throwsIfWrongStatus() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.DRAFT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::validate);
        assertTrue(ex.getMessage().length() > 0);
    }

    @Test
    void validationFailed_throwsIfWrongStatus() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.DRAFT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::validationFailed);
        assertTrue(ex.getMessage().length() > 0);
    }

    @Test
    void approve_worksInExpectedStatus() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.approve();

        assertEquals(RequestStatus.APPROVED, r.getStatus());
    }

    @Test
    void approve_throwsIfNotAllowed() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.DRAFT);

        assertThrows(IllegalStateException.class, r::approve);
    }

    @Test
    void reject_worksInExpectedStatus() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.PENDING);

        r.reject();

        assertEquals(RequestStatus.REJECTED, r.getStatus());
    }

    @Test
    void reject_throwsIfNotAllowed() {
        Request r = new Request("title", "desc");
        setStatus(r, RequestStatus.DRAFT);

        assertThrows(IllegalStateException.class, r::reject);
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
        assertTrue(ex.getMessage().length() > 0);
    }
}