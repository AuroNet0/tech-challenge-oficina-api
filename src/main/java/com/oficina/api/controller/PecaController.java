package com.oficina.api.controller;

import com.oficina.api.dto.request.peca.AtualizarEstoqueRequest;
import com.oficina.api.dto.request.peca.PecaCreateRequest;
import com.oficina.api.dto.request.peca.PecaUpdateRequest;
import com.oficina.api.dto.response.peca.PecaResponse;
import com.oficina.api.model.Peca;
import com.oficina.api.service.PecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Peças e Insumos", description = "Gestão de peças, insumos e estoque")
@SecurityRequirement(name = "bearerAuth")
public class PecaController {

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @PostMapping
    @Operation(summary = "Criar peça", description = "Cadastra uma nova peça ou insumo no estoque.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito de dados, como duplicidade")
    })
    public ResponseEntity<PecaResponse> criar(@Valid @RequestBody PecaCreateRequest request) {
        Peca peca = pecaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(peca));
    }

    @GetMapping
    @Operation(summary = "Listar peças", description = "Retorna todas as peças e insumos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<PecaResponse>> listar() {
        List<PecaResponse> response = pecaService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativas")
    @Operation(summary = "Listar peças ativas", description = "Retorna somente as peças e insumos ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<PecaResponse>> listarAtivas() {
        List<PecaResponse> response = pecaService.listarAtivas().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Listar peças com estoque baixo", description = "Retorna peças com quantidade em estoque abaixo do valor informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<PecaResponse>> listarComEstoqueBaixo(@RequestParam Integer quantidade) {
        List<PecaResponse> response = pecaService.listarComEstoqueBaixo(quantidade).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar peça por ID", description = "Consulta uma peça específica pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable Long id) {
        Peca peca = pecaService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(peca));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar peça", description = "Atualiza dados cadastrais de uma peça existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<PecaResponse> atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody PecaUpdateRequest request) {
        Peca peca = pecaService.atualizar(id, request);
        return ResponseEntity.ok(toResponse(peca));
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Atualizar estoque", description = "Atualiza a quantidade em estoque de uma peça.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<PecaResponse> atualizarEstoque(@PathVariable Long id,
                                                         @Valid @RequestBody AtualizarEstoqueRequest request) {
        Peca peca = pecaService.atualizarEstoque(id, request);
        return ResponseEntity.ok(toResponse(peca));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir peça", description = "Remove uma peça ou insumo da base.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurso removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
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
