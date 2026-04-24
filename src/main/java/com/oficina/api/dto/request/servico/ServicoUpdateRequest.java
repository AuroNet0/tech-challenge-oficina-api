package com.oficina.api.dto.request.servico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoUpdateRequest(
        @NotBlank(message = "Descricao e obrigatoria.")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @NotNull(message = "Valor e obrigatorio.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Valor deve ser maior ou igual a zero.")
        BigDecimal valor,

        @NotNull(message = "Tempo estimado e obrigatorio.")
        @Min(value = 1, message = "Tempo estimado deve ser no minimo 1 minuto.")
        Integer tempoEstimadoMinutos,

        @NotNull(message = "Ativo e obrigatorio.")
        Boolean ativo
) {
}
