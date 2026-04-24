package com.oficina.api.dto.response.ordemservico;

import com.oficina.api.model.enums.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long veiculoId,
        String veiculoPlaca,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFinalizacao,
        BigDecimal valorTotal,
        String observacoes,
        List<ItemServicoOrdemResponse> itensServico,
        List<ItemPecaOrdemResponse> itensPeca,
        OrcamentoResponse orcamento
) {
}
