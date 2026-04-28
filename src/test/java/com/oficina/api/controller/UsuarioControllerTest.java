package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficina.api.dto.request.usuario.UsuarioCreateRequest;
import com.oficina.api.dto.request.usuario.UsuarioUpdateRequest;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Usuario;
import com.oficina.api.model.enums.PerfilUsuario;
import com.oficina.api.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postUsuariosRetorna201() throws Exception {
        when(usuarioService.criar(any(UsuarioCreateRequest.class))).thenReturn(criarUsuario(1L, "usuario@email.com"));
        UsuarioCreateRequest request = new UsuarioCreateRequest("Usuario", "usuario@email.com", "123456", PerfilUsuario.ATENDENTE);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Usuario 1"))
                .andExpect(jsonPath("$.email").value("usuario@email.com"))
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void getUsuariosRetorna200() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(
                criarUsuario(1L, "a@email.com"),
                criarUsuario(2L, "b@email.com")
        ));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUsuarioPorIdRetorna200() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(criarUsuario(1L, "a@email.com"));

        mockMvc.perform(get("/usuarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("a@email.com"));
    }

    @Test
    void putUsuarioRetorna200() throws Exception {
        when(usuarioService.atualizar(eq(1L), any(UsuarioUpdateRequest.class)))
                .thenReturn(criarUsuario(1L, "atualizado@email.com"));
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Nome Atualizado", "atualizado@email.com", PerfilUsuario.GERENTE, true);

        mockMvc.perform(put("/usuarios/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("atualizado@email.com"));
    }

    @Test
    void deleteUsuarioRetorna204() throws Exception {
        doNothing().when(usuarioService).excluir(1L);

        mockMvc.perform(delete("/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private Usuario criarUsuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setEmail(email);
        usuario.setSenha("senha-nao-exposta");
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        usuario.setAtivo(true);
        return usuario;
    }
}
