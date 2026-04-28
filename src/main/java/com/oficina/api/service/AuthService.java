
package com.oficina.api.service;

import com.oficina.api.dto.request.auth.LoginRequest;
import com.oficina.api.dto.response.auth.LoginResponse;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.model.Usuario;
import com.oficina.api.repository.UsuarioRepository;
import com.oficina.api.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));

        boolean senhaValida = passwordEncoder.matches(request.senha(), usuario.getSenha());
        if (!senhaValida) {
            throw new BusinessException("Senha invalida.");
        }

        String token = jwtUtil.generateToken(usuario);
        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }
}
