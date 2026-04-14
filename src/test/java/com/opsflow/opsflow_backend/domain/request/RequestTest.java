package com.opsflow.opsflow_backend.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void constructor_setsDraftAndCreatedAt() {
        Request r = new Request("t", "desc");
        assertEquals(RequestStatus.DRAFT, r.getStatus());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void submit_movesDraftToValidated() {
        Request r = new Request("title", "desc");
        assertEquals(RequestStatus.DRAFT, r.getStatus());

        r.submit();

        assertEquals(RequestStatus.VALIDATED, r.getStatus());
    }

    @Test
    void submit_throwsIfNotDraft() {
        Request r = new Request("title", "desc");
        r.submit(); // ahora VALIDATED

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::submit);
        assertTrue(ex.getMessage().contains("Only DRAFT"));
    }

    @Test
    void validate_movesValidatedToPending() {
        Request r = new Request("title", "desc");
        r.submit(); // VALIDATED

        r.validate();

        assertEquals(RequestStatus.PENDING, r.getStatus());
    }

    @Test
    void validate_throwsIfNotValidated() {
        Request r = new Request("title", "desc"); // DRAFT

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::validate);
        assertTrue(ex.getMessage().contains("Only VALIDATED"));
    }

    @Test
    void approve_movesPendingToApproved() {
        Request r = new Request("title", "desc");
        r.submit();
        r.validate(); // PENDING

        r.approve();

        assertEquals(RequestStatus.APPROVED, r.getStatus());
    }

    @Test
    void reject_movesPendingToRejected() {
        Request r = new Request("title", "desc");
        r.submit();
        r.validate(); // PENDING

        r.reject();

        assertEquals(RequestStatus.REJECTED, r.getStatus());
    }

    @Test
    void approve_throwsIfNotPending() {
        Request r = new Request("title", "desc");
        r.submit(); // VALIDATED

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::approve);
        assertTrue(ex.getMessage().contains("Only PENDING"));
    }

    @Test
    void reject_throwsIfNotPending() {
        Request r = new Request("title", "desc");
        r.submit(); // VALIDATED

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::reject);
        assertTrue(ex.getMessage().contains("Only PENDING"));
    }

    @Test
    void retry_movesRejectedToDraft() {
        Request r = new Request("title", "desc");
        r.submit();
        r.validate();
        r.reject(); // REJECTED

        r.retry();

        assertEquals(RequestStatus.DRAFT, r.getStatus());
    }

    @Test
    void retry_throwsIfNotRejected() {
        Request r = new Request("title", "desc");

        IllegalStateException ex = assertThrows(IllegalStateException.class, r::retry);
        assertTrue(ex.getMessage().contains("Only REJECTED"));
    }

    @Test
    void approve_whenNotPending_shouldFail() {
        Request r = new Request("t", "desc ok");
        r.submit(); // VALIDATED

        assertThrows(IllegalStateException.class, r::approve);
    }

    @Test
    void reject_whenNotPending_shouldFail() {
        Request r = new Request("t", "desc ok");

        assertThrows(IllegalStateException.class, r::reject);
    }

    @Test
    void retry_whenNotRejected_shouldFail() {
        Request r = new Request("t", "desc ok");

        assertThrows(IllegalStateException.class, r::retry);
    }
}