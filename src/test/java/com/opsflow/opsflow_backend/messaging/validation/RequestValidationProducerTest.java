package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestValidationProducerTest {

    @Test
    void send_shouldPublishMessageToValidationQueue() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RequestValidationProducer producer = new RequestValidationProducer(rabbitTemplate);

        producer.send(9L, "REQ-9", "iago");

        ArgumentCaptor<RequestValidationMessage> captor =
                ArgumentCaptor.forClass(RequestValidationMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REQUEST_VALIDATION_QUEUE),
                captor.capture()
        );

        RequestValidationMessage msg = captor.getValue();
        assertNotNull(msg);
        assertEquals(9L, msg.requestId());
        assertEquals("REQ-9", msg.requestCode());
        assertEquals("iago", msg.requestedBy());
        assertNotNull(msg.messageId());
        assertNotNull(msg.correlationId());
    }
}