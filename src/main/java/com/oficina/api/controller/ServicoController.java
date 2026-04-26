package com.oficina.api.controller;

import com.oficina.api.dto.request.servico.ServicoCreateRequest;
import com.oficina.api.dto.request.servico.ServicoUpdateRequest;
import com.oficina.api.dto.response.servico.ServicoResponse;
import com.oficina.api.model.Servico;
import com.oficina.api.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoCreateRequest request) {
        Servico servico = servicoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(servico));
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar() {
        List<ServicoResponse> response = servicoService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ServicoResponse>> listarAtivos() {
        List<ServicoResponse> response = servicoService.listarAtivos().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        Servico servico = servicoService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(servico));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ServicoUpdateRequest request) {
        Servico servico = servicoService.atualizar(id, request);
        return ResponseEntity.ok(toResponse(servico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getDescricao(),
                servico.getValor(),
                servico.getTempoEstimadoMinutos(),
                servico.getAtivo()
        );
    }
}
