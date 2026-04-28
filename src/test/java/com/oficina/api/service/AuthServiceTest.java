
package com.oficina.api.service;

import com.oficina.api.dto.request.auth.LoginRequest;
import com.oficina.api.dto.response.auth.LoginResponse;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.model.Usuario;
import com.oficina.api.model.enums.PerfilUsuario;
import com.oficina.api.repository.UsuarioRepository;
import com.oficina.api.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveAutenticarUsuarioComSucesso() {
        LoginRequest request = new LoginRequest("usuario@email.com", "123456");
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "senha-criptografada")).thenReturn(true);
        when(jwtUtil.generateToken(usuario)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.usuarioId()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Usuario Teste");
        assertThat(response.email()).isEqualTo("usuario@email.com");
        assertThat(response.perfil()).isEqualTo(PerfilUsuario.ATENDENTE);
        verify(usuarioRepository).findByEmail("usuario@email.com");
        verify(passwordEncoder).matches("123456", "senha-criptografada");
        verify(jwtUtil).generateToken(usuario);
    }

    @Test
    void deveLancarBusinessExceptionSeUsuarioNaoExistir() {
        LoginRequest request = new LoginRequest("inexistente@email.com", "123456");
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertThat(exception.getMessage()).isEqualTo("Usuario nao encontrado.");
        verify(usuarioRepository).findByEmail("inexistente@email.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(Usuario.class));
    }

    @Test
    void deveLancarBusinessExceptionSeSenhaEstiverIncorreta() {
        LoginRequest request = new LoginRequest("usuario@email.com", "senha-incorreta");
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-incorreta", "senha-criptografada")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertThat(exception.getMessage()).isEqualTo("Senha invalida.");
        verify(usuarioRepository).findByEmail("usuario@email.com");
        verify(passwordEncoder).matches("senha-incorreta", "senha-criptografada");
        verify(jwtUtil, never()).generateToken(usuario);
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        usuario.setAtivo(true);
        return usuario;
    }
}
