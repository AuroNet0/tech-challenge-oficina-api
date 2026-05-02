package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdemServicoPublicControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private OrdemServicoPublicController ordemServicoPublicController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

    @Test
    void deveAprovarOrcamentoPublicoComSucesso() throws Exception {
        when(ordemServicoService.aprovarOrcamentoCliente(eq("token-abc"), eq(1L), any(AprovarOrcamentoRequest.class)))
                .thenReturn(criarOrdemServico(1L));

        mockMvc.perform(patch("/public/ordens-servico/{id}/orcamento", 1L)
                        .param("token", "token-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AprovarOrcamentoRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
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
