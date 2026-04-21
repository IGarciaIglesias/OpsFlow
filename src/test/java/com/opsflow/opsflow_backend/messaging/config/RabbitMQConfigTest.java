package com.opsflow.opsflow_backend.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    @Test
    void queues_shouldHaveExpectedNames() {
        RabbitMQConfig config = new RabbitMQConfig();

        assertEquals(RabbitMQConfig.REQUEST_VALIDATION_QUEUE, config.requestValidationQueue().getName());
        assertEquals(RabbitMQConfig.REQUEST_VALIDATION_DLQ, config.requestValidationDlq().getName());
        assertEquals(RabbitMQConfig.REQUEST_EXECUTION_QUEUE, config.requestExecutionQueue().getName());
        assertEquals(RabbitMQConfig.REQUEST_NOTIFICATIONS_QUEUE, config.requestNotificationsQueue().getName());
    }

    @Test
    void requestValidationQueue_shouldHaveDlqArguments() {
        RabbitMQConfig config = new RabbitMQConfig();
        Queue queue = config.requestValidationQueue();

        Map<String, Object> args = queue.getArguments();
        assertEquals("", args.get("x-dead-letter-exchange"));
        assertEquals(RabbitMQConfig.REQUEST_VALIDATION_DLQ, args.get("x-dead-letter-routing-key"));
    }

    @Test
    void jsonMessageConverter_shouldNotBeNull() {
        RabbitMQConfig config = new RabbitMQConfig();
        assertNotNull(config.jsonMessageConverter());
    }

    @Test
    void rabbitTemplate_shouldUseProvidedConverter() {
        RabbitMQConfig config = new RabbitMQConfig();
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);

        assertSame(converter, template.getMessageConverter());
    }
}