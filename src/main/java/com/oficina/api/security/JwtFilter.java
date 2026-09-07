
package com.oficina.api.security;

import com.oficina.api.model.Usuario;
import com.oficina.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isValidToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String subject = jwtUtil.extractUsername(token);
                String tipo = jwtUtil.extractTipo(token);

                if ("CLIENTE".equals(tipo)) {
                    authenticateCliente(request, subject);
                } else {
                    usuarioRepository.findByEmail(subject)
                            .ifPresent(usuario -> authenticateUsuario(request, usuario));
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUsuario(HttpServletRequest request, Usuario usuario) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateCliente(HttpServletRequest request, String cpf) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                cpf,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
