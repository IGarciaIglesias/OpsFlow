package com.opsflow.opsflow_backend.domain.request.event;

import com.opsflow.opsflow_backend.domain.request.Request;

public class RequestRejectedEvent {

    private final Request request;

    public RequestRejectedEvent(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}