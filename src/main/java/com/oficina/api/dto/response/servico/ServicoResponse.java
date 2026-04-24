package com.oficina.api.dto.response.servico;

import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        Integer tempoEstimadoMinutos,
        Boolean ativo
) {
}
