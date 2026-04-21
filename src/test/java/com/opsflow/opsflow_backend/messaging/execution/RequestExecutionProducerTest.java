package com.opsflow.opsflow_backend.messaging.execution;

import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestExecutionProducerTest {

    @Test
    void send_shouldPublishMessageToExecutionQueue() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RequestExecutionProducer producer = new RequestExecutionProducer(rabbitTemplate);

        producer.send(7L, "REQ-7", "iago");

        ArgumentCaptor<RequestExecutionMessage> captor =
                ArgumentCaptor.forClass(RequestExecutionMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REQUEST_EXECUTION_QUEUE),
                captor.capture()
        );

        RequestExecutionMessage msg = captor.getValue();
        assertNotNull(msg);
        assertEquals(7L, msg.requestId());
        assertEquals("REQ-7", msg.requestCode());
        assertEquals("iago", msg.requestedBy());
        assertNotNull(msg.messageId());
        assertNotNull(msg.correlationId());
    }
}