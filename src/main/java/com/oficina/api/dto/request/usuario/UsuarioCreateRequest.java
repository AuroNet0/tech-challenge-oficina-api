package com.oficina.api.dto.request.usuario;

import com.oficina.api.model.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        String nome,

        @NotBlank(message = "Email e obrigatorio.")
        @Email(message = "Email invalido.")
        String email,

        @NotBlank(message = "Senha e obrigatoria.")
        @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres.")
        String senha,

        @NotNull(message = "Perfil e obrigatorio.")
        PerfilUsuario perfil
) {
}
