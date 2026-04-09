package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.domain.request.RequestHistory;
import com.opsflow.opsflow_backend.domain.request.RequestStatus;
import com.opsflow.opsflow_backend.domain.request.event.RequestApprovedEvent;
import com.opsflow.opsflow_backend.domain.request.event.RequestRejectedEvent;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestHistoryRepository;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationMessage;
import com.opsflow.opsflow_backend.messaging.validation.RequestValidationConsumer;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestValidationConsumer validationConsumer;

    public RequestController(
            RequestRepository requestRepository,
            RequestHistoryRepository historyRepository,
            ApplicationEventPublisher eventPublisher,
            RequestValidationConsumer validationConsumer
    ) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
        this.validationConsumer = validationConsumer;
    }

    // Crear solicitud
    @PostMapping
    public ResponseEntity<RequestResponseDto> create(
            @Valid @RequestBody CreateRequestDto dto
    ) {
        Request request = new Request(dto.title(), dto.description());
        Request saved = requestRepository.save(request);

        return ResponseEntity
                .created(URI.create("/requests/" + saved.getId()))
                .body(RequestResponseDto.from(saved));
    }

    // Obtener solicitud por id
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getById(
            @PathVariable Long id
    ) {
        return requestRepository.findById(id)
                .map(RequestResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Listar todas las solicitudes
    @GetMapping
    public List<RequestResponseDto> getAll() {
        return requestRepository.findAll()
                .stream()
                .map(RequestResponseDto::from)
                .toList();
    }

    // Aprobar solicitud
    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestResponseDto> approve(@PathVariable Long id) {

        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.approve();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(
                            new RequestHistory(saved, from, RequestStatus.APPROVED)
                    );

                    eventPublisher.publishEvent(
                            new RequestApprovedEvent(saved)
                    );

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Rechazar solicitud
    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestResponseDto> reject(@PathVariable Long id) {

        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.reject();
                    Request saved = requestRepository.save(request);

                    historyRepository.save(
                            new RequestHistory(saved, from, RequestStatus.REJECTED)
                    );

                    eventPublisher.publishEvent(
                            new RequestRejectedEvent(saved)
                    );

                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public List<RequestResponseDto.RequestHistoryDto> history(@PathVariable Long id) {
        return historyRepository
                .findByRequestIdOrderByChangedAtAsc(id)
                .stream()
                .map(RequestResponseDto.RequestHistoryDto::from)
                .toList();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.submit();
                    Request saved = requestRepository.save(request);
                    historyRepository.save(
                            new RequestHistory(
                                    saved,
                                    from,
                                    saved.getStatus()
                            )
                    );
                    // ✅ Simulación de publicación
                    RequestValidationMessage message =
                            RequestValidationMessage.of(saved.getId());
                    validationConsumer.consume(message);
                    System.out.println(
                            "📤 Published validation message: " + message
                    );
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validate(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.validate();
                    Request saved = requestRepository.save(request);
                    historyRepository.save(
                            new RequestHistory(saved, from, saved.getStatus())
                    );
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.cancel();
                    Request saved = requestRepository.save(request);
                    historyRepository.save(
                            new RequestHistory(saved, from, saved.getStatus())
                    );
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retry(@PathVariable Long id) {
        return requestRepository.findById(id)
                .map(request -> {
                    RequestStatus from = request.getStatus();
                    request.retry();
                    Request saved = requestRepository.save(request);
                    historyRepository.save(
                            new RequestHistory(saved, from, saved.getStatus())
                    );
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
