package com.opsflow.opsflow_backend.application.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.execution.RequestExecutionProducer;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationProducer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestWorkflowService {

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;
    private final RequestValidationProducer validationProducer;
    private final RequestExecutionProducer executionProducer;

    public RequestWorkflowService(
            RequestRepository requestRepository,
            RequestHistoryRepository historyRepository,
            RequestValidationProducer validationProducer,
            RequestExecutionProducer executionProducer
    ) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.validationProducer = validationProducer;
        this.executionProducer = executionProducer;
    }

    @Transactional
    public Request submit(Long id, String username) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted");
        }

        RequestStatus from = request.getStatus();
        request.submitForValidation();

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

        validationProducer.send(saved.getId(), saved.getCode(), username);

        return saved;
    }

    @Transactional
    public Request approve(Long id, String username) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));

        if (request.getStatus() != RequestStatus.VALIDATED) {
            throw new IllegalStateException("Only VALIDATED requests can be approved");
        }

        RequestStatus from = request.getStatus();
        request.approve();

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

        executionProducer.send(saved.getId(), saved.getCode(), username);

        return saved;
    }

    @Transactional
    public Request reject(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));

        RequestStatus from = request.getStatus();
        request.reject();

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

        return saved;
    }

    @Transactional
    public Request cancel(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));

        RequestStatus from = request.getStatus();
        request.cancel();

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

        return saved;
    }

    @Transactional
    public Request retry(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));

        RequestStatus from = request.getStatus();
        request.retry();

        Request saved = requestRepository.save(request);
        historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

        return saved;
    }
}