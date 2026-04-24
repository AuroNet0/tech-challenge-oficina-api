package com.oficina.api.dto.response.ordemservico;

import java.math.BigDecimal;

public record OrcamentoResponse(
        BigDecimal valorServicos,
        BigDecimal valorPecas,
        BigDecimal valorTotal
) {
}
