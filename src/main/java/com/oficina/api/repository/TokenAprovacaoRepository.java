package com.oficina.api.repository;

import com.oficina.api.model.TokenAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenAprovacaoRepository extends JpaRepository<TokenAprovacao, Long> {

    Optional<TokenAprovacao> findByToken(String token);
}
