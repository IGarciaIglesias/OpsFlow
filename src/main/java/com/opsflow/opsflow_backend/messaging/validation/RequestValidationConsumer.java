package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer de validación técnica asíncrona.
 *
 * Flujo:
 * DRAFT → VALIDATED   (controller)
 * VALIDATED → PENDING (Rabbit)
 *
 * NO hay validación KO
 * NO hay lógica de negocio
 */
@Component
public class RequestValidationConsumer {

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;

    public RequestValidationConsumer(
            RequestRepository requestRepository,
            RequestHistoryRepository historyRepository
    ) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.REQUEST_VALIDATION_QUEUE)
    @Transactional
    public void consume(RequestValidationMessage message) {

        Request request = requestRepository.findById(message.requestId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Request not found: " + message.requestId()
                        )
                );

        // ✅ Solo VALIDATED
        if (request.getStatus() != RequestStatus.VALIDATED) {
            return;
        }

        RequestStatus from = request.getStatus();

        // ✅ VALIDATED → PENDING (SIEMPRE)
        request.validate();

        Request saved = requestRepository.save(request);

        historyRepository.save(
                new RequestHistory(saved, from, saved.getStatus())
        );

        System.out.println(
                "[Rabbit] Request " + saved.getId() + " → PENDING"
        );
    }
}