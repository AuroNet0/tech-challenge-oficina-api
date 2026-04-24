package com.oficina.api.dto.request.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarOrdemServicoRequest(
        @NotNull(message = "Cliente e obrigatorio.")
        Long clienteId,

        @NotNull(message = "Veiculo e obrigatorio.")
        Long veiculoId,

        @Size(max = 1000, message = "Observacoes deve ter no maximo 1000 caracteres.")
        String observacoes
) {
}
