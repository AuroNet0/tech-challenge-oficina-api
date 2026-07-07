package com.oficina.api.service;

import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.TokenAprovacao;
import com.oficina.api.repository.TokenAprovacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenAprovacaoService {

    private static final long HORAS_EXPIRACAO = 48;

    private final TokenAprovacaoRepository tokenAprovacaoRepository;

    public TokenAprovacaoService(TokenAprovacaoRepository tokenAprovacaoRepository) {
        this.tokenAprovacaoRepository = tokenAprovacaoRepository;
    }

    @Transactional
    public TokenAprovacao gerarToken(OrdemServico ordemServico) {
        TokenAprovacao tokenAprovacao = new TokenAprovacao();
        tokenAprovacao.setToken(UUID.randomUUID().toString());
        tokenAprovacao.setOrdemServico(ordemServico);
        tokenAprovacao.setExpiracao(LocalDateTime.now().plusHours(HORAS_EXPIRACAO));
        tokenAprovacao.setUtilizado(false);
        return tokenAprovacaoRepository.save(tokenAprovacao);
    }

    @Transactional(readOnly = true)
    public TokenAprovacao validarToken(String token) {
        TokenAprovacao tokenAprovacao = tokenAprovacaoRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de aprovacao nao encontrado."));

        if (tokenAprovacao.isUtilizado()) {
            throw new BusinessException("Token de aprovacao ja utilizado.");
        }

        if (tokenAprovacao.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de aprovacao expirado.");
        }

        return tokenAprovacao;
    }

    @Transactional
    public void marcarComoUtilizado(TokenAprovacao tokenAprovacao) {
        tokenAprovacao.setUtilizado(true);
        tokenAprovacao.setUtilizadoEm(LocalDateTime.now());
        tokenAprovacaoRepository.save(tokenAprovacao);
    }
}
