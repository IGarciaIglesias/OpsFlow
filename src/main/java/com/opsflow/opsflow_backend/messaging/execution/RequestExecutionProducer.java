package com.opsflow.opsflow_backend.messaging.execution;

import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RequestExecutionProducer {

    private static final Logger log = LoggerFactory.getLogger(RequestExecutionProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RequestExecutionProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Long requestId, String requestCode, String requestedBy) {
        RequestExecutionMessage message = RequestExecutionMessage.of(requestId, requestCode, requestedBy);

        rabbitTemplate.convertAndSend(RabbitMQConfig.REQUEST_EXECUTION_QUEUE, message);

        log.info("[Rabbit][execution] Sent messageId={}, correlationId={}, requestId={}",
                message.messageId(), message.correlationId(), message.requestId());
    }
}