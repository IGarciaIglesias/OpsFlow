package com.opsflow.opsflow_backend.messaging.execution;

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
public class RequestExecutionConsumer {

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
    public void consume(RequestExecutionMessage message) throws InterruptedException {
        Request request = requestRepository.findById(message.requestId())
                .orElseThrow(() -> new IllegalStateException("Request not found: " + message.requestId()));

        if (request.getStatus() != RequestStatus.APPROVED) {
            return;
        }

        RequestStatus from = request.getStatus();
        request.startExecution();
        Request inProgress = requestRepository.save(request);
        historyRepository.save(new RequestHistory(inProgress, from, inProgress.getStatus()));

        Thread.sleep(2000);

        RequestStatus fromExecution = inProgress.getStatus();

        boolean ok = true;

        if (ok) {
            inProgress.completeExecution();
        } else {
            inProgress.failExecution();
        }

        Request finished = requestRepository.save(inProgress);
        historyRepository.save(new RequestHistory(finished, fromExecution, finished.getStatus()));
    }
}