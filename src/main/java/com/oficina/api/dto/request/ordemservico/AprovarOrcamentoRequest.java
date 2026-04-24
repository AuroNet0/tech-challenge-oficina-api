package com.oficina.api.dto.request.ordemservico;

import jakarta.validation.constraints.NotNull;

public record AprovarOrcamentoRequest(
        @NotNull(message = "Campo aprovado e obrigatorio.")
        Boolean aprovado
) {
}
