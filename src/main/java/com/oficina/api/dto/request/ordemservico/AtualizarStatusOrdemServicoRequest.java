package com.oficina.api.dto.request.ordemservico;

import com.oficina.api.model.enums.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrdemServicoRequest(
        @NotNull(message = "Status e obrigatorio.")
        StatusOrdemServico status
) {
}
