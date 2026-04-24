package com.oficina.api.dto.request.peca;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PecaCreateRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Descricao e obrigatoria.")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @NotNull(message = "Valor unitario e obrigatorio.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Valor unitario deve ser maior ou igual a zero.")
        BigDecimal valorUnitario,

        @NotNull(message = "Quantidade em estoque e obrigatoria.")
        @Min(value = 0, message = "Quantidade em estoque deve ser maior ou igual a zero.")
        Integer quantidadeEstoque
) {
}
