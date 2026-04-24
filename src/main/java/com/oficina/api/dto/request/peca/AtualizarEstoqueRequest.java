package com.oficina.api.dto.request.peca;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarEstoqueRequest(
        @NotNull(message = "Quantidade e obrigatoria.")
        @Min(value = 0, message = "Quantidade deve ser maior ou igual a zero.")
        Integer quantidade
) {
}
