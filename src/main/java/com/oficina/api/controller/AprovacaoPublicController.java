package com.oficina.api.controller;

import com.oficina.api.dto.response.ordemservico.OrdemServicoResponse;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.service.AprovacaoOrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;

@RestController
@Validated
@RequestMapping("/public/aprovacoes")
@Tag(name = "Aprovacao de Orcamento", description = "Aprovacao ou reprovacao de orcamento por link publico")
public class AprovacaoPublicController {

    private final AprovacaoOrcamentoService aprovacaoOrcamentoService;

    public AprovacaoPublicController(AprovacaoOrcamentoService aprovacaoOrcamentoService) {
        this.aprovacaoOrcamentoService = aprovacaoOrcamentoService;
    }

    @RequestMapping(value = "/{token}/aprovar", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Aprovar orcamento por token", description = "Valida o token recebido por e-mail e aprova o orcamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacao realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido, expirado, utilizado ou regra de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Token nao encontrado")
    })
    public ResponseEntity<OrdemServicoResponse> aprovar(@PathVariable String token) {
        OrdemServico ordemServico = aprovacaoOrcamentoService.aprovar(token);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    @RequestMapping(value = "/{token}/reprovar", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Reprovar orcamento por token", description = "Valida o token recebido por e-mail e reprova o orcamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacao realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido, expirado, utilizado ou regra de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Token nao encontrado")
    })
    public ResponseEntity<OrdemServicoResponse> reprovar(@PathVariable String token) {
        OrdemServico ordemServico = aprovacaoOrcamentoService.reprovar(token);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    private OrdemServicoResponse toResponse(OrdemServico ordemServico) {
        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getCliente() != null ? ordemServico.getCliente().getId() : null,
                ordemServico.getCliente() != null ? ordemServico.getCliente().getNome() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getId() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getPlaca() : null,
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataFinalizacao(),
                ordemServico.getValorTotal() == null ? BigDecimal.ZERO : ordemServico.getValorTotal(),
                ordemServico.getObservacoes(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
    }
}
