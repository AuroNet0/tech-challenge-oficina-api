package com.oficina.api.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email e obrigatorio.")
        @Email(message = "Email invalido.")
        @Size(max = 120, message = "Email deve ter no maximo 120 caracteres.")
        String email,

        @NotBlank(message = "Senha e obrigatoria.")
        @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres.")
        String senha
) {
}
