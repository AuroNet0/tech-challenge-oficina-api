package com.oficina.api.dto.response.ordemservico;

import java.math.BigDecimal;

public record ItemPecaOrdemResponse(
        Long id,
        Long pecaId,
        String nomePeca,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {
}
