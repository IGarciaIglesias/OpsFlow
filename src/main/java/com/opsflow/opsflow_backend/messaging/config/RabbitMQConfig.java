package com.opsflow.opsflow_backend.messaging.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REQUEST_VALIDATION_QUEUE = "request.validation";
    public static final String REQUEST_VALIDATION_DLQ = "request.validation.dlq";

    // ✅ Cola principal con DLQ
    @Bean
    public Queue requestValidationQueue() {
        return QueueBuilder.durable(REQUEST_VALIDATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", REQUEST_VALIDATION_DLQ)
                .build();
    }

    // ✅ Cola de muertos
    @Bean
    public Queue requestValidationDlq() {
        return new Queue(REQUEST_VALIDATION_DLQ, true);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
