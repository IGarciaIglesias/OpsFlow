package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RequestValidationProducer {

    private static final Logger log = LoggerFactory.getLogger(RequestValidationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RequestValidationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Long requestId, String requestCode, String requestedBy) {
        RequestValidationMessage message = RequestValidationMessage.of(requestId, requestCode, requestedBy);

        rabbitTemplate.convertAndSend(RabbitMQConfig.REQUEST_VALIDATION_QUEUE, message);

        log.info("[Rabbit][validation] Sent messageId={}, correlationId={}, requestId={}",
                message.messageId(), message.correlationId(), message.requestId());
    }
}