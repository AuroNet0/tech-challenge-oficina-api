package com.oficina.api.dto.request.veiculo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoCreateRequest(
        @NotBlank(message = "Placa e obrigatoria.")
        @Size(min = 7, max = 8, message = "Placa deve ter entre 7 e 8 caracteres.")
        String placa,

        @NotBlank(message = "Marca e obrigatoria.")
        @Size(max = 60, message = "Marca deve ter no maximo 60 caracteres.")
        String marca,

        @NotBlank(message = "Modelo e obrigatorio.")
        @Size(max = 80, message = "Modelo deve ter no maximo 80 caracteres.")
        String modelo,

        @NotNull(message = "Ano e obrigatorio.")
        @Min(value = 1886, message = "Ano invalido.")
        @Max(value = 3000, message = "Ano invalido.")
        Integer ano,

        @NotNull(message = "Cliente e obrigatorio.")
        Long clienteId
) {
}
