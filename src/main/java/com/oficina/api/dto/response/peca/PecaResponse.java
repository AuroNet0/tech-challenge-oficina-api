package com.oficina.api.dto.response.peca;

import java.math.BigDecimal;

public record PecaResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Boolean ativo
) {
}
