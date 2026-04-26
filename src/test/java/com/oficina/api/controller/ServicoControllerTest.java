package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.servico.ServicoCreateRequest;
import com.oficina.api.dto.request.servico.ServicoUpdateRequest;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Servico;
import com.oficina.api.service.ServicoService;
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
class ServicoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController servicoController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(servicoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postServicosRetorna201() throws Exception {
        Servico servico = criarServico(1L, true);
        when(servicoService.criar(any(ServicoCreateRequest.class))).thenReturn(servico);

        ServicoCreateRequest request = new ServicoCreateRequest("Troca de oleo", new BigDecimal("120.00"), 60);

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Troca de oleo"));
    }

    @Test
    void getServicosRetorna200() throws Exception {
        when(servicoService.listar()).thenReturn(List.of(criarServico(1L, true), criarServico(2L, false)));

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getServicosAtivosRetorna200() throws Exception {
        when(servicoService.listarAtivos()).thenReturn(List.of(criarServico(1L, true)));

        mockMvc.perform(get("/servicos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ativo").value(true));
    }

    @Test
    void getServicoPorIdRetorna200() throws Exception {
        when(servicoService.buscarPorId(1L)).thenReturn(criarServico(1L, true));

        mockMvc.perform(get("/servicos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Troca de oleo"));
    }

    @Test
    void putServicoRetorna200() throws Exception {
        when(servicoService.atualizar(eq(1L), any(ServicoUpdateRequest.class))).thenReturn(criarServico(1L, false));

        ServicoUpdateRequest request = new ServicoUpdateRequest("Alinhamento", new BigDecimal("200.00"), 90, false);

        mockMvc.perform(put("/servicos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void deleteServicoRetorna204() throws Exception {
        doNothing().when(servicoService).excluir(1L);

        mockMvc.perform(delete("/servicos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private Servico criarServico(Long id, Boolean ativo) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setDescricao("Troca de oleo");
        servico.setValor(new BigDecimal("120.00"));
        servico.setTempoEstimadoMinutos(60);
        servico.setAtivo(ativo);
        return servico;
    }
}
