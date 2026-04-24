package com.oficina.api.dto.response.ordemservico;

import java.math.BigDecimal;

public record ItemServicoOrdemResponse(
        Long id,
        Long servicoId,
        String descricaoServico,
        BigDecimal valor,
        String observacao
) {
}
