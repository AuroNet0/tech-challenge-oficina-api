package com.oficina.api.controller;

import com.oficina.api.dto.request.veiculo.VeiculoCreateRequest;
import com.oficina.api.dto.request.veiculo.VeiculoUpdateRequest;
import com.oficina.api.dto.response.veiculo.VeiculoResponse;
import com.oficina.api.model.Veiculo;
import com.oficina.api.service.VeiculoService;
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
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoCreateRequest request) {
        Veiculo veiculo = veiculoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(veiculo));
    }

    @GetMapping
    public ResponseEntity<List<VeiculoResponse>> listar() {
        List<VeiculoResponse> response = veiculoService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(veiculo));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        List<VeiculoResponse> response = veiculoService.listarPorCliente(clienteId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody VeiculoUpdateRequest request) {
        Veiculo veiculo = veiculoService.atualizar(id, request);
        return ResponseEntity.ok(toResponse(veiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        veiculoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private VeiculoResponse toResponse(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCliente() != null ? veiculo.getCliente().getId() : null,
                veiculo.getCliente() != null ? veiculo.getCliente().getNome() : null
        );
    }
}
