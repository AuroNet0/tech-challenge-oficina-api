package com.oficina.api.dto.request.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteUpdateRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Telefone e obrigatorio.")
        @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres.")
        String telefone,

        @Email(message = "Email invalido.")
        @Size(max = 120, message = "Email deve ter no maximo 120 caracteres.")
        String email
) {
}
