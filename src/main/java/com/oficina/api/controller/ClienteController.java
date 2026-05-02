package com.oficina.api.controller;

import com.oficina.api.dto.request.cliente.ClienteCreateRequest;
import com.oficina.api.dto.request.cliente.ClienteUpdateRequest;
import com.oficina.api.dto.response.cliente.ClienteResponse;
import com.oficina.api.model.Cliente;
import com.oficina.api.service.ClienteService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Gestão de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cadastra um novo cliente na base da oficina.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito de dados, como duplicidade")
    })
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteCreateRequest request) {
        Cliente cliente = clienteService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cliente));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna a lista de clientes cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<ClienteResponse>> listar() {
        List<ClienteResponse> response = clienteService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(cliente));
    }

    @GetMapping("/cpf-cnpj/{cpfCnpj}")
    @Operation(summary = "Buscar cliente por CPF/CNPJ", description = "Retorna os dados de um cliente a partir do CPF ou CNPJ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacao realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF/CNPJ invalido"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado")
    })
    public ResponseEntity<ClienteResponse> buscarPorCpfOuCnpj(@PathVariable String cpfCnpj) {
        Cliente cliente = clienteService.buscarPorCpfOuCnpj(cpfCnpj);
        return ResponseEntity.ok(toResponse(cliente));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados cadastrais de um cliente existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ClienteUpdateRequest request) {
        Cliente cliente = clienteService.atualizar(id, request);
        return ResponseEntity.ok(toResponse(cliente));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Remove um cliente cadastrado da base.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurso removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTipoPessoa(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getTokenAcesso(),
                cliente.getDataCadastro()
        );
    }
}
