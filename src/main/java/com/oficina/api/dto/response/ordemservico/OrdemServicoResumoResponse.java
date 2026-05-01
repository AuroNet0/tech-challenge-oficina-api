package com.oficina.api.dto.response.ordemservico;

import com.oficina.api.model.enums.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResumoResponse(
        Long id,
        String clienteNome,
        String veiculoPlaca,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        BigDecimal valorTotal,
        BigDecimal tempoExecucaoHoras
) {
}
