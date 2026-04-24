package com.oficina.api.dto.response.cliente;

import com.oficina.api.model.enums.TipoPessoa;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        TipoPessoa tipoPessoa,
        String telefone,
        String email,
        LocalDateTime dataCadastro
) {
}
