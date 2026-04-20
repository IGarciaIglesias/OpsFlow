package com.opsflow.opsflow_backend.messaging.execution;

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
public class RequestExecutionConsumer {

    private static final Logger log = LoggerFactory.getLogger(RequestExecutionConsumer.class);

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;

    public RequestExecutionConsumer(
            RequestRepository requestRepository,
            RequestHistoryRepository historyRepository
    ) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.REQUEST_EXECUTION_QUEUE)
    @Transactional
    public void consume(RequestExecutionMessage message) {
        log.info("[Rabbit][execution] Received messageId={}, correlationId={}, requestId={}",
                message.messageId(), message.correlationId(), message.requestId());

        Request request = requestRepository.findById(message.requestId())
                .orElseThrow(() ->
                        new IllegalStateException("Request not found: " + message.requestId())
                );

        if (request.getStatus() != RequestStatus.APPROVED) {
            log.warn("[Rabbit][execution] Ignored request {} because status is {}",
                    request.getId(), request.getStatus());
            return;
        }

        RequestStatus fromApproved = request.getStatus();
        request.startExecution();

        Request inProgress = requestRepository.save(request);
        historyRepository.save(new RequestHistory(inProgress, fromApproved, inProgress.getStatus()));

        boolean success = simulateExecution(inProgress);

        RequestStatus fromExecution = inProgress.getStatus();

        if (success) {
            inProgress.completeExecution();
            log.info("[Rabbit][execution] Request {} completed successfully", inProgress.getId());
        } else {
            inProgress.failExecution();
            log.warn("[Rabbit][execution] Request {} failed during execution", inProgress.getId());
        }

        Request finished = requestRepository.save(inProgress);
        historyRepository.save(new RequestHistory(finished, fromExecution, finished.getStatus()));
    }

    private boolean simulateExecution(Request request) {
        String description = request.getDescription();
        return description == null || !description.toLowerCase().contains("force-fail");
    }
}