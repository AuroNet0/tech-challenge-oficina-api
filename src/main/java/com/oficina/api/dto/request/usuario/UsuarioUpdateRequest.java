package com.oficina.api.dto.request.usuario;

import com.oficina.api.model.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioUpdateRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        String nome,

        @NotBlank(message = "Email e obrigatorio.")
        @Email(message = "Email invalido.")
        String email,

        @NotNull(message = "Perfil e obrigatorio.")
        PerfilUsuario perfil,

        @NotNull(message = "Ativo e obrigatorio.")
        Boolean ativo
) {
}
