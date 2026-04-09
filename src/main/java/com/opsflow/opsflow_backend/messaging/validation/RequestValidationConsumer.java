package com.opsflow.opsflow_backend.messaging.validation;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
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

    @Transactional
    public void consume(RequestValidationMessage message) {

        System.out.println("📥 Processing validation message: " + message);

        requestRepository.findById(message.requestId())
                .ifPresent(request -> {

                    RequestStatus from = request.getStatus();

                    // Simulación de validación OK
                    request.validate();

                    Request saved = requestRepository.save(request);

                    historyRepository.save(
                            new RequestHistory(
                                    saved,
                                    from,
                                    saved.getStatus()
                            )
                    );

                    System.out.println(
                            "Request validated: " + saved.getId()
                    );
                });
    }
}
