package com.opsflow.opsflow_backend.domain.request.event;

import com.opsflow.opsflow_backend.domain.request.Request;

public class RequestApprovedEvent {

    private final Request request;

    public RequestApprovedEvent(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}