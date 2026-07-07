package com.oficina.api.service;

import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.TokenAprovacao;
import com.oficina.api.model.enums.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AprovacaoOrcamentoServiceTest {

    @Mock
    private TokenAprovacaoService tokenAprovacaoService;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private AprovacaoOrcamentoService aprovacaoOrcamentoService;

    @Test
    void deveAprovarReutilizandoRegraDaOrdemServicoEMarcarTokenComoUtilizado() {
        TokenAprovacao tokenAprovacao = criarTokenAprovacao();
        OrdemServico ordemAprovada = criarOrdemServico(StatusOrdemServico.EM_EXECUCAO);
        when(tokenAprovacaoService.validarToken("token-abc")).thenReturn(tokenAprovacao);
        when(ordemServicoService.aprovarOrcamento(any(), any(AprovarOrcamentoRequest.class))).thenReturn(ordemAprovada);

        OrdemServico resultado = aprovacaoOrcamentoService.aprovar("token-abc");

        ArgumentCaptor<AprovarOrcamentoRequest> requestCaptor = ArgumentCaptor.forClass(AprovarOrcamentoRequest.class);
        assertThat(resultado).isEqualTo(ordemAprovada);
        verify(ordemServicoService).aprovarOrcamento(org.mockito.ArgumentMatchers.eq(100L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().aprovado()).isTrue();
        verify(tokenAprovacaoService).marcarComoUtilizado(tokenAprovacao);
    }

    @Test
    void deveReprovarReutilizandoRegraDaOrdemServicoEMarcarTokenComoUtilizado() {
        TokenAprovacao tokenAprovacao = criarTokenAprovacao();
        OrdemServico ordemReprovada = criarOrdemServico(StatusOrdemServico.CANCELADA);
        when(tokenAprovacaoService.validarToken("token-abc")).thenReturn(tokenAprovacao);
        when(ordemServicoService.aprovarOrcamento(any(), any(AprovarOrcamentoRequest.class))).thenReturn(ordemReprovada);

        OrdemServico resultado = aprovacaoOrcamentoService.reprovar("token-abc");

        ArgumentCaptor<AprovarOrcamentoRequest> requestCaptor = ArgumentCaptor.forClass(AprovarOrcamentoRequest.class);
        assertThat(resultado).isEqualTo(ordemReprovada);
        verify(ordemServicoService).aprovarOrcamento(org.mockito.ArgumentMatchers.eq(100L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().aprovado()).isFalse();
        verify(tokenAprovacaoService).marcarComoUtilizado(tokenAprovacao);
    }

    @Test
    void naoDeveMarcarTokenQuandoRegraDaOrdemServicoFalhar() {
        TokenAprovacao tokenAprovacao = criarTokenAprovacao();
        when(tokenAprovacaoService.validarToken("token-abc")).thenReturn(tokenAprovacao);
        when(ordemServicoService.aprovarOrcamento(any(), any(AprovarOrcamentoRequest.class)))
                .thenThrow(new BusinessException("A ordem de servico deve estar AGUARDANDO_APROVACAO para aprovacao."));

        assertThrows(BusinessException.class, () -> aprovacaoOrcamentoService.aprovar("token-abc"));

        verify(tokenAprovacaoService, never()).marcarComoUtilizado(any());
    }

    private TokenAprovacao criarTokenAprovacao() {
        TokenAprovacao tokenAprovacao = new TokenAprovacao();
        tokenAprovacao.setToken("token-abc");
        tokenAprovacao.setOrdemServico(criarOrdemServico(StatusOrdemServico.AGUARDANDO_APROVACAO));
        return tokenAprovacao;
    }

    private OrdemServico criarOrdemServico(StatusOrdemServico status) {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(100L);
        ordemServico.setStatus(status);
        return ordemServico;
    }
}
