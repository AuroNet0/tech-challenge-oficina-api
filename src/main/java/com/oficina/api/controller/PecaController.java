package com.oficina.api.controller;

import com.oficina.api.dto.request.peca.AtualizarEstoqueRequest;
import com.oficina.api.dto.request.peca.PecaCreateRequest;
import com.oficina.api.dto.request.peca.PecaUpdateRequest;
import com.oficina.api.dto.response.peca.PecaResponse;
import com.oficina.api.model.Peca;
import com.oficina.api.service.PecaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/pecas")
public class PecaController {

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @PostMapping
    public ResponseEntity<PecaResponse> criar(@Valid @RequestBody PecaCreateRequest request) {
        Peca peca = pecaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(peca));
    }

    @GetMapping
    public ResponseEntity<List<PecaResponse>> listar() {
        List<PecaResponse> response = pecaService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<PecaResponse>> listarAtivas() {
        List<PecaResponse> response = pecaService.listarAtivas().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<PecaResponse>> listarComEstoqueBaixo(@RequestParam Integer quantidade) {
        List<PecaResponse> response = pecaService.listarComEstoqueBaixo(quantidade).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable Long id) {
        Peca peca = pecaService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(peca));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponse> atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody PecaUpdateRequest request) {
        Peca peca = pecaService.atualizar(id, request);
        return ResponseEntity.ok(toResponse(peca));
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<PecaResponse> atualizarEstoque(@PathVariable Long id,
                                                         @Valid @RequestBody AtualizarEstoqueRequest request) {
        Peca peca = pecaService.atualizarEstoque(id, request);
        return ResponseEntity.ok(toResponse(peca));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pecaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private PecaResponse toResponse(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getValorUnitario(),
                peca.getQuantidadeEstoque(),
                peca.getAtivo()
        );
    }
}
