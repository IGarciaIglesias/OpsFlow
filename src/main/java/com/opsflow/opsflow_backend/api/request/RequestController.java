package com.opsflow.opsflow_backend.api.request;

import com.opsflow.opsflow_backend.domain.request.Request;
import com.opsflow.opsflow_backend.infrastructure.persistence.request.RequestRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestRepository requestRepository;

    public RequestController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
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
                    request.approve();
                    Request saved = requestRepository.save(request);
                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Rechazar solicitud
    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestResponseDto> reject(@PathVariable Long id) {

        return requestRepository.findById(id)
                .map(request -> {
                    request.reject();
                    Request saved = requestRepository.save(request);
                    return ResponseEntity.ok(RequestResponseDto.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
