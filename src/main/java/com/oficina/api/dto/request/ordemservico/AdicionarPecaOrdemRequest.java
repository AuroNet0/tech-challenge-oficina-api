package com.oficina.api.dto.request.ordemservico;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdicionarPecaOrdemRequest(
        @NotNull(message = "Peca e obrigatoria.")
        Long pecaId,

        @NotNull(message = "Quantidade e obrigatoria.")
        @Min(value = 1, message = "Quantidade deve ser no minimo 1.")
        Integer quantidade
) {
}
