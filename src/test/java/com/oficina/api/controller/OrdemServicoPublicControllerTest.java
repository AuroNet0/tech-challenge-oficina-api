package com.oficina.api.controller;

import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.Veiculo;
import com.oficina.api.model.enums.StatusOrdemServico;
import com.oficina.api.service.OrdemServicoService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdemServicoPublicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private OrdemServicoPublicController ordemServicoPublicController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(ordemServicoPublicController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void deveListarOrdensPorTokenComSucesso() throws Exception {
        when(ordemServicoService.listarPorTokenCliente("token-abc")).thenReturn(List.of(criarOrdemServico(1L)));

        mockMvc.perform(get("/public/ordens-servico")
                        .param("token", "token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente 1"));
    }

    private OrdemServico criarOrdemServico(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Cliente 1");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setPlaca("ABC1D23");

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(id);
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 5, 2, 10, 0));
        ordemServico.setValorTotal(BigDecimal.ZERO);
        return ordemServico;
    }
}
