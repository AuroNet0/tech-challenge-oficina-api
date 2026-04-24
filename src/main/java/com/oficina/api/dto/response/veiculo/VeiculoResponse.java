package com.oficina.api.dto.response.veiculo;

public record VeiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId,
        String clienteNome
) {
}
