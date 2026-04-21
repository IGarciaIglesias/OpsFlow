package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.api.common.PageResponseDto;
import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.event.RequestApprovedEvent;
import com.opsflow.opsflow_backend.domain.request.event.RequestRejectedEvent;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.config.RabbitMQConfig;
import com.opsflow.opsflow_backend.messaging.execution.RequestExecutionMessage;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationMessage;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public RequestController(
            RequestRepository requestRepository,
            RequestHistoryRepository historyRepository,
            ApplicationEventPublisher eventPublisher,
            RabbitTemplate rabbitTemplate
    ) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<RequestResponseDto> create(@Valid @RequestBody CreateRequestDto dto) {
        Request request = new Request(
                dto.title(),
                dto.description(),
                dto.creator(),
                dto.assignee(),
                dto.priority(),
                dto.type()
        );

        Request saved = requestRepository.save(request);

        return ResponseEntity
                .created(URI.create("/requests/" + saved.getId()))
                .body(RequestResponseDto.from(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getById(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(RequestResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<RequestResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) RequestStatus status
    ) {
        Sort.Direction direction = Sort.Direction.ASC;
        String sortBy = "id";

        if (sort.length > 0) {
            sortBy = sort[0];
        }
        if (sort.length > 1) {
            direction = Sort.Direction.fromString(sort[1]);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RequestResponseDto> result = (status != null)
                ? requestRepository.findByStatus(status, pageable).map(RequestResponseDto::from)
                : requestRepository.findAll(pageable).map(RequestResponseDto::from);

        return ResponseEntity.ok(PageResponseDto.from(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestDto dto
    ) {
        return requestRepository.findById(id)
                .map(request -> {
                    request.updateDraft(
                            dto.title(),
                            dto.description(),
                            dto.creator(),
                            dto.assignee(),
                            dto.priority(),
                            dto.type()
                    );

                    Request saved = requestRepository.save(request);
                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<RequestResponseDto> submit(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    if (request.getStatus() != RequestStatus.DRAFT) {
                        return ResponseEntity.badRequest().<RequestResponseDto>build();
                    }

                    RequestStatus from = request.getStatus();
                    request.submitForValidation();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.REQUEST_VALIDATION_QUEUE,
                            RequestValidationMessage.of(
                                    saved.getId(),
                                    UUID.randomUUID().toString(),
                                    UUID.randomUUID().toString()
                            )
                    );

                    return ResponseEntity.accepted().body(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestResponseDto> approve(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.approve();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));
                    eventPublisher.publishEvent(new RequestApprovedEvent(saved));

                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.REQUEST_EXECUTION_QUEUE,
                            RequestExecutionMessage.of(
                                    saved.getId(),
                                    UUID.randomUUID().toString(),
                                    UUID.randomUUID().toString()
                            )
                    );

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestResponseDto> reject(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.reject();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));
                    eventPublisher.publishEvent(new RequestRejectedEvent(saved));

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RequestResponseDto> cancel(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.cancel();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<RequestResponseDto> retry(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.retry();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(new RequestHistory(saved, from, saved.getStatus()));

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<RequestResponseDto.RequestHistoryDto>> history(@PathVariable Long id) {
        if (!requestRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<RequestResponseDto.RequestHistoryDto> history = historyRepository
                .findByRequestIdOrderByChangedAtAsc(id)
                .stream()
                .map(RequestResponseDto.RequestHistoryDto::from)
                .toList();

        return ResponseEntity.ok(history);
    }
}