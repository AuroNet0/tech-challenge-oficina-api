package com.oficina.api.dto.response.auth;

import com.oficina.api.model.enums.PerfilUsuario;

public record LoginResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nome,
        String email,
        PerfilUsuario perfil
) {
}
