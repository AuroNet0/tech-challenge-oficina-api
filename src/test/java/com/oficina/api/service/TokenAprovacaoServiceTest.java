package com.oficina.api.service;

import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.TokenAprovacao;
import com.oficina.api.repository.TokenAprovacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAprovacaoServiceTest {

    @Mock
    private TokenAprovacaoRepository tokenAprovacaoRepository;

    @InjectMocks
    private TokenAprovacaoService tokenAprovacaoService;

    @Test
    void deveGerarTokenComExpiracaoEOrdemServico() {
        OrdemServico ordemServico = new OrdemServico();
        when(tokenAprovacaoRepository.save(any(TokenAprovacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenAprovacao resultado = tokenAprovacaoService.gerarToken(ordemServico);

        assertThat(resultado.getToken()).isNotBlank();
        assertThat(resultado.getOrdemServico()).isEqualTo(ordemServico);
        assertThat(resultado.getExpiracao()).isAfter(LocalDateTime.now());
        assertThat(resultado.isUtilizado()).isFalse();
        verify(tokenAprovacaoRepository).save(any(TokenAprovacao.class));
    }

    @Test
    void deveValidarTokenComSucesso() {
        TokenAprovacao tokenAprovacao = criarToken(false, LocalDateTime.now().plusHours(1));
        when(tokenAprovacaoRepository.findByToken("token-abc")).thenReturn(Optional.of(tokenAprovacao));

        TokenAprovacao resultado = tokenAprovacaoService.validarToken("token-abc");

        assertThat(resultado).isEqualTo(tokenAprovacao);
        verify(tokenAprovacaoRepository).findByToken("token-abc");
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoTokenNaoExistir() {
        when(tokenAprovacaoRepository.findByToken("token-invalido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tokenAprovacaoService.validarToken("token-invalido"));

        verify(tokenAprovacaoRepository).findByToken("token-invalido");
    }

    @Test
    void deveLancarBusinessExceptionQuandoTokenJaUtilizado() {
        when(tokenAprovacaoRepository.findByToken("token-abc"))
                .thenReturn(Optional.of(criarToken(true, LocalDateTime.now().plusHours(1))));

        assertThrows(BusinessException.class, () -> tokenAprovacaoService.validarToken("token-abc"));
    }

    @Test
    void deveLancarBusinessExceptionQuandoTokenExpirado() {
        when(tokenAprovacaoRepository.findByToken("token-abc"))
                .thenReturn(Optional.of(criarToken(false, LocalDateTime.now().minusMinutes(1))));

        assertThrows(BusinessException.class, () -> tokenAprovacaoService.validarToken("token-abc"));
    }

    @Test
    void deveMarcarTokenComoUtilizado() {
        TokenAprovacao tokenAprovacao = criarToken(false, LocalDateTime.now().plusHours(1));

        tokenAprovacaoService.marcarComoUtilizado(tokenAprovacao);

        assertThat(tokenAprovacao.isUtilizado()).isTrue();
        assertThat(tokenAprovacao.getUtilizadoEm()).isNotNull();
        verify(tokenAprovacaoRepository).save(tokenAprovacao);
    }

    private TokenAprovacao criarToken(boolean utilizado, LocalDateTime expiracao) {
        TokenAprovacao tokenAprovacao = new TokenAprovacao();
        tokenAprovacao.setToken("token-abc");
        tokenAprovacao.setOrdemServico(new OrdemServico());
        tokenAprovacao.setUtilizado(utilizado);
        tokenAprovacao.setExpiracao(expiracao);
        return tokenAprovacao;
    }
}
