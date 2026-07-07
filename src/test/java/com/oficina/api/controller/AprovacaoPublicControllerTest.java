package com.oficina.api.controller;

import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.Veiculo;
import com.oficina.api.model.enums.StatusOrdemServico;
import com.oficina.api.service.AprovacaoOrcamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AprovacaoPublicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AprovacaoOrcamentoService aprovacaoOrcamentoService;

    @InjectMocks
    private AprovacaoPublicController aprovacaoPublicController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(aprovacaoPublicController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void deveAprovarOrcamentoPorTokenComGet() throws Exception {
        when(aprovacaoOrcamentoService.aprovar("token-abc"))
                .thenReturn(criarOrdemServico(StatusOrdemServico.EM_EXECUCAO));

        mockMvc.perform(get("/public/aprovacoes/{token}/aprovar", "token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));

        verify(aprovacaoOrcamentoService).aprovar("token-abc");
    }

    @Test
    void deveReprovarOrcamentoPorTokenComPost() throws Exception {
        when(aprovacaoOrcamentoService.reprovar("token-abc"))
                .thenReturn(criarOrdemServico(StatusOrdemServico.CANCELADA));

        mockMvc.perform(post("/public/aprovacoes/{token}/reprovar", "token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        verify(aprovacaoOrcamentoService).reprovar("token-abc");
    }

    @Test
    void deveRetornar400QuandoTokenExpirado() throws Exception {
        when(aprovacaoOrcamentoService.aprovar("token-expirado"))
                .thenThrow(new BusinessException("Token de aprovacao expirado."));

        mockMvc.perform(post("/public/aprovacoes/{token}/aprovar", "token-expirado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Token de aprovacao expirado."));
    }

    @Test
    void deveRetornar404QuandoTokenNaoExistir() throws Exception {
        when(aprovacaoOrcamentoService.reprovar("token-invalido"))
                .thenThrow(new ResourceNotFoundException("Token de aprovacao nao encontrado."));

        mockMvc.perform(post("/public/aprovacoes/{token}/reprovar", "token-invalido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Token de aprovacao nao encontrado."));
    }

    private OrdemServico criarOrdemServico(StatusOrdemServico status) {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Cliente 1");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setPlaca("ABC1D23");

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(100L);
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setStatus(status);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 5, 2, 10, 0));
        ordemServico.setValorTotal(new BigDecimal("170.00"));
        return ordemServico;
    }
}
