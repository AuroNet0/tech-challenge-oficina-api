package com.oficina.api.security;

import com.oficina.api.controller.AuthController;
import com.oficina.api.controller.OrdemServicoController;
import com.oficina.api.repository.UsuarioRepository;
import com.oficina.api.service.AuthService;
import com.oficina.api.service.OrdemServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, OrdemServicoController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void roleClienteNaoDeveAcessarRotaProtegidaPelaRegraFinal() throws Exception {
        mockMvc.perform(get("/rota-final"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void roleFuncionarioDevePassarPelaRegraFinal() throws Exception {
        mockMvc.perform(get("/rota-final"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void endpointPermitAllContinuaAcessivelSemAutenticacao() throws Exception {
        mockMvc.perform(get("/auth/inexistente"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void healthcheckActuatorContinuaAcessivelSemAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void roleClienteDeveAcessarMinhasOrdens() throws Exception {
        mockMvc.perform(get("/ordens-servico/minhas"))
                .andExpect(status().isOk());
    }

    @Test
    void requisicaoSemAutenticacaoNaoDeveAcessarMinhasOrdens() throws Exception {
        mockMvc.perform(get("/ordens-servico/minhas"))
                .andExpect(status().isForbidden());
    }
}
