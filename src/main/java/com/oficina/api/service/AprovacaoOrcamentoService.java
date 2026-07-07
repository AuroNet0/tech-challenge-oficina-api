package com.oficina.api.service;

import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.TokenAprovacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AprovacaoOrcamentoService {

    private final TokenAprovacaoService tokenAprovacaoService;
    private final OrdemServicoService ordemServicoService;

    public AprovacaoOrcamentoService(TokenAprovacaoService tokenAprovacaoService,
                                     OrdemServicoService ordemServicoService) {
        this.tokenAprovacaoService = tokenAprovacaoService;
        this.ordemServicoService = ordemServicoService;
    }

    @Transactional
    public OrdemServico aprovar(String token) {
        return decidir(token, true);
    }

    @Transactional
    public OrdemServico reprovar(String token) {
        return decidir(token, false);
    }

    private OrdemServico decidir(String token, boolean aprovado) {
        TokenAprovacao tokenAprovacao = tokenAprovacaoService.validarToken(token);
        OrdemServico ordemServico = tokenAprovacao.getOrdemServico();
        OrdemServico resultado = ordemServicoService.aprovarOrcamento(
                ordemServico.getId(),
                new AprovarOrcamentoRequest(aprovado)
        );
        tokenAprovacaoService.marcarComoUtilizado(tokenAprovacao);
        return resultado;
    }
}
