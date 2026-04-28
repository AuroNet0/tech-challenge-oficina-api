package com.oficina.api.service;

import com.oficina.api.dto.request.usuario.UsuarioCreateRequest;
import com.oficina.api.dto.request.usuario.UsuarioUpdateRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.model.Usuario;
import com.oficina.api.model.enums.PerfilUsuario;
import com.oficina.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        lenient().when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        UsuarioCreateRequest request = new UsuarioCreateRequest("Usuario", "usuario@email.com", "123456", PerfilUsuario.ATENDENTE);
        when(usuarioRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");

        Usuario resultado = usuarioService.criar(request);

        assertThat(resultado.getNome()).isEqualTo("Usuario");
        assertThat(resultado.getEmail()).isEqualTo("usuario@email.com");
        assertThat(resultado.getSenha()).isEqualTo("senha-criptografada");
        assertThat(resultado.getPerfil()).isEqualTo(PerfilUsuario.ATENDENTE);
        assertThat(resultado.getAtivo()).isTrue();
        verify(usuarioRepository).existsByEmail("usuario@email.com");
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void criarDeveCriptografarSenha() {
        UsuarioCreateRequest request = new UsuarioCreateRequest("Usuario", "usuario@email.com", "123456", PerfilUsuario.ATENDENTE);
        when(usuarioRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");

        Usuario resultado = usuarioService.criar(request);

        assertThat(resultado.getSenha()).isEqualTo("senha-criptografada");
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void erroAoCriarComEmailDuplicado() {
        UsuarioCreateRequest request = new UsuarioCreateRequest("Usuario", "usuario@email.com", "123456", PerfilUsuario.ATENDENTE);
        when(usuarioRepository.existsByEmail("usuario@email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> usuarioService.criar(request));

        verify(usuarioRepository).existsByEmail("usuario@email.com");
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void buscarPorIdComSucesso() {
        Usuario usuario = criarUsuario(1L, "a@email.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(usuario);
        verify(usuarioRepository).findById(1L);
    }

    @Test
    void erroAoBuscarIdInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarPorId(1L));

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void listarUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                criarUsuario(1L, "a@email.com"),
                criarUsuario(2L, "b@email.com")
        ));

        List<Usuario> resultado = usuarioService.listar();

        assertThat(resultado).hasSize(2);
        verify(usuarioRepository).findAll();
    }

    @Test
    void atualizarComSucesso() {
        Usuario usuario = criarUsuario(1L, "atual@email.com");
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Novo Nome", "novo@email.com", PerfilUsuario.GERENTE, false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);

        Usuario resultado = usuarioService.atualizar(1L, request);

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        assertThat(resultado.getEmail()).isEqualTo("novo@email.com");
        assertThat(resultado.getPerfil()).isEqualTo(PerfilUsuario.GERENTE);
        assertThat(resultado.getAtivo()).isFalse();
        assertThat(resultado.getSenha()).isEqualTo("senha-criptografada");
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).existsByEmail("novo@email.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void erroAoAtualizarUsuarioInexistente() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Novo Nome", "novo@email.com", PerfilUsuario.GERENTE, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.atualizar(1L, request));

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void erroAoAtualizarComEmailDuplicadoDeOutroUsuario() {
        Usuario usuario = criarUsuario(1L, "atual@email.com");
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Novo Nome", "duplicado@email.com", PerfilUsuario.GERENTE, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("duplicado@email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> usuarioService.atualizar(1L, request));

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).existsByEmail("duplicado@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void excluirComSucesso() {
        Usuario usuario = criarUsuario(1L, "excluir@email.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.excluir(1L);

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void erroAoExcluirUsuarioInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.excluir(1L));

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    private Usuario criarUsuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setEmail(email);
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        usuario.setAtivo(true);
        return usuario;
    }
}
