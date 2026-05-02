package com.oficina.api.security;

import com.oficina.api.model.Usuario;
import com.oficina.api.model.enums.PerfilUsuario;
import com.oficina.api.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAtribuirRoleGerenteNoSecurityContext() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);
        Usuario usuario = criarUsuario(PerfilUsuario.GERENTE);

        when(jwtUtil.isValidToken("token-gerente")).thenReturn(true);
        when(jwtUtil.extractUsername("token-gerente")).thenReturn("gerente@email.com");
        when(usuarioRepository.findByEmail("gerente@email.com")).thenReturn(Optional.of(usuario));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-gerente");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_GERENTE");
    }

    @Test
    void deveAtribuirRoleMecanicoNoSecurityContext() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);
        Usuario usuario = criarUsuario(PerfilUsuario.MECANICO);

        when(jwtUtil.isValidToken("token-mecanico")).thenReturn(true);
        when(jwtUtil.extractUsername("token-mecanico")).thenReturn("mecanico@email.com");
        when(usuarioRepository.findByEmail("mecanico@email.com")).thenReturn(Optional.of(usuario));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-mecanico");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_MECANICO");
    }

    @Test
    void naoDeveAutenticarSemHeaderAuthorization() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);

        jwtFilter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private Usuario criarUsuario(PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuario;
    }
}
