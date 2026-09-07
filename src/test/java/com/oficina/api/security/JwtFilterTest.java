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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void tokenFuncionarioSemTipoDeveConsultarUsuarioRepositoryEAtribuirRoleDoPerfil() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);
        Usuario usuario = criarUsuario(PerfilUsuario.GERENTE);

        when(jwtUtil.isValidToken("token-gerente")).thenReturn(true);
        when(jwtUtil.extractUsername("token-gerente")).thenReturn("gerente@email.com");
        when(jwtUtil.extractTipo("token-gerente")).thenReturn(null);
        when(usuarioRepository.findByEmail("gerente@email.com")).thenReturn(Optional.of(usuario));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-gerente");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("teste@email.com");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_GERENTE");
        verify(usuarioRepository).findByEmail("gerente@email.com");
    }

    @Test
    void tokenClienteDeveAutenticarComCpfERoleClienteSemConsultarUsuarioRepository() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);

        when(jwtUtil.isValidToken("token-cliente")).thenReturn(true);
        when(jwtUtil.extractUsername("token-cliente")).thenReturn("12345678901");
        when(jwtUtil.extractTipo("token-cliente")).thenReturn("CLIENTE");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-cliente");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("12345678901");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_CLIENTE");
        verify(usuarioRepository, never()).findByEmail("12345678901");
    }

    @Test
    void tokenInvalidoNaoDeveCriarAuthenticationNoSecurityContext() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);

        when(jwtUtil.isValidToken("token-invalido")).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).extractUsername("token-invalido");
        verify(jwtUtil, never()).extractTipo("token-invalido");
    }

    @Test
    void tokenValidoComTipoDiferenteDeClienteDeveManterFluxoLegadoDeFuncionario() throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);
        Usuario usuario = criarUsuario(PerfilUsuario.MECANICO);

        when(jwtUtil.isValidToken("token-mecanico")).thenReturn(true);
        when(jwtUtil.extractUsername("token-mecanico")).thenReturn("mecanico@email.com");
        when(jwtUtil.extractTipo("token-mecanico")).thenReturn("FUNCIONARIO");
        when(usuarioRepository.findByEmail("mecanico@email.com")).thenReturn(Optional.of(usuario));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-mecanico");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("teste@email.com");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_MECANICO");
        verify(usuarioRepository).findByEmail("mecanico@email.com");
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
