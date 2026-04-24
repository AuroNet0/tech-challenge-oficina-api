package com.oficina.api.dto.request.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdicionarServicoOrdemRequest(
        @NotNull(message = "Servico e obrigatorio.")
        Long servicoId,

        @Size(max = 1000, message = "Observacao deve ter no maximo 1000 caracteres.")
        String observacao
) {
}
