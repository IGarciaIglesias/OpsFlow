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
                        new IllegalStateException("Request not found: " + message.requestId())
                );

        // Solo procesamos solicitudes en VALIDATED
        if (request.getStatus() != RequestStatus.VALIDATED) {
            return;
        }

        RequestStatus from = request.getStatus();

        // ✅ Validación técnica mínima (objetiva)
        boolean ok = isTechnicallyValid(request);

        if (ok) {
            // VALIDATED -> PENDING
            request.validate();
            System.out.println("[Rabbit] Validation OK for request " + request.getId() + " -> PENDING");
        } else {
            // VALIDATED -> REJECTED
            request.validationFailed();
            System.out.println("[Rabbit] Validation KO for request " + request.getId() + " -> REJECTED");
        }

        Request saved = requestRepository.save(request);

        historyRepository.save(
                new RequestHistory(saved, from, saved.getStatus())
        );
    }

    private boolean isTechnicallyValid(Request request) {
        // Regla 1: title no vacío y mínimo 3 chars
        String title = request.getTitle();
        if (title == null || title.trim().length() < 3) return false;

        // Regla 2: description no vacía y mínimo 5 chars
        String desc = request.getDescription();
        if (desc == null || desc.trim().length() < 5) return false;

        // Regla 3: description no excede el límite lógico del modelo (1000)
        if (desc.length() > 1000) return false;

        return true;
    }
}
