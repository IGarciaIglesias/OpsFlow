package com.opsflow.opsflow_backend.domain.request.listener;

import com.opsflow.opsflow_backend.domain.request.event.RequestApprovedEvent;
import com.opsflow.opsflow_backend.domain.request.event.RequestRejectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RequestDomainEventListener {

    @EventListener
    public void onApproved(RequestApprovedEvent event) {
        System.out.println(
                "Request approved: " + event.getRequest().getId()
        );
    }

    @EventListener
    public void onRejected(RequestRejectedEvent event) {
        System.out.println(
                "Request rejected: " + event.getRequest().getId()
        );
    }
}
