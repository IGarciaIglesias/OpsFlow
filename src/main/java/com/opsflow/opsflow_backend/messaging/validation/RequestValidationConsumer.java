package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RequestValidationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RequestValidationConsumer.class);

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
        log.info("[Rabbit][validation] Received messageId={}, correlationId={}, requestId={}",
                message.messageId(), message.correlationId(), message.requestId());

        Request request = requestRepository.findById(message.requestId())
                .orElseThrow(() ->
                        new IllegalStateException("Request not found: " + message.requestId())
                );

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn("[Rabbit][validation] Ignored request {} because status is {}",
                    request.getId(), request.getStatus());
            return;
        }

        RequestStatus from = request.getStatus();
        ValidationDecision decision = evaluate(request);

        switch (decision) {
            case VALID -> {
                request.validate();
                log.info("[Rabbit][validation] Request {} validated successfully", request.getId());
            }
            case REJECT -> {
                request.reject();
                log.info("[Rabbit][validation] Request {} rejected by validation rules", request.getId());
            }
            case FAIL -> {
                request.validationFailed();
                log.warn("[Rabbit][validation] Request {} failed technical validation", request.getId());
            }
        }

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));
    }

    private ValidationDecision evaluate(Request request) {
        String title = normalize(request.getTitle());
        String description = normalize(request.getDescription());

        if (title.length() < 3 || title.length() > 120) {
            return ValidationDecision.REJECT;
        }

        if (description.length() < 10 || description.length() > 1000) {
            return ValidationDecision.REJECT;
        }

        if (containsSpamKeywords(title, description)) {
            return ValidationDecision.REJECT;
        }

        if (hasTooManyUrls(description)) {
            return ValidationDecision.REJECT;
        }

        if (hasExcessiveRepetition(description)) {
            return ValidationDecision.REJECT;
        }

        if (containsOnlyNoise(description)) {
            return ValidationDecision.FAIL;
        }

        return ValidationDecision.VALID;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private boolean containsSpamKeywords(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        return text.contains("buy now")
                || text.contains("free money")
                || text.contains("click here")
                || text.contains("winner")
                || text.contains("urgent response")
                || text.contains("viagra");
    }

    private boolean hasTooManyUrls(String text) {
        int count = text.split("https?://", -1).length - 1;
        return count > 2;
    }

    private boolean hasExcessiveRepetition(String text) {
        return text.matches(".*(.)\\1{6,}.*");
    }

    private boolean containsOnlyNoise(String text) {
        String cleaned = text.replaceAll("[^a-zA-Z0-9]", "");
        return cleaned.length() < 5;
    }

    private enum ValidationDecision {
        VALID,
        REJECT,
        FAIL
    }
}