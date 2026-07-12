package com.oficina.api.controller;

import com.oficina.api.dto.response.ordemservico.OrdemServicoResumoResponse;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@RestController
@Validated
@RequestMapping("/public/ordens-servico")
@Tag(name = "Acompanhamento do Cliente", description = "Consulta de status pelo cliente")
public class OrdemServicoPublicController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoPublicController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @GetMapping
    @Operation(summary = "Listar ordens do cliente", description = "Retorna as ordens de servico do cliente a partir do token de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacao realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado")
    })
    public ResponseEntity<List<OrdemServicoResumoResponse>> listarPorClienteToken(@RequestParam String token) {
        List<OrdemServico> ordens = ordemServicoService.listarPorTokenCliente(token);
        List<OrdemServicoResumoResponse> response = ordens == null ? Collections.emptyList() : ordens.stream()
                .map(this::toResumoResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private OrdemServicoResumoResponse toResumoResponse(OrdemServico ordemServico) {
        return new OrdemServicoResumoResponse(
                ordemServico.getId(),
                ordemServico.getCliente() != null ? ordemServico.getCliente().getNome() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getPlaca() : null,
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                nullSafe(ordemServico.getValorTotal()),
                calcularTempoExecucaoHoras(ordemServico)
        );
    }

    private BigDecimal calcularTempoExecucaoHoras(OrdemServico ordemServico) {
        if (ordemServico.getDataInicioExecucao() == null || ordemServico.getDataFinalizacao() == null) {
            return null;
        }

        long minutos = Duration.between(ordemServico.getDataInicioExecucao(), ordemServico.getDataFinalizacao()).toMinutes();
        if (minutos < 0) {
            return null;
        }

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullSafe(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
