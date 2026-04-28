package com.oficina.api.dto.response.usuario;

import com.oficina.api.model.enums.PerfilUsuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo
) {
}
