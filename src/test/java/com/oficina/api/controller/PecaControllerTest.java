package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.peca.AtualizarEstoqueRequest;
import com.oficina.api.dto.request.peca.PecaCreateRequest;
import com.oficina.api.dto.request.peca.PecaUpdateRequest;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Peca;
import com.oficina.api.service.PecaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PecaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PecaService pecaService;

    @InjectMocks
    private PecaController pecaController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(pecaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postPecasRetorna201() throws Exception {
        when(pecaService.criar(any(PecaCreateRequest.class))).thenReturn(criarPeca(1L));
        PecaCreateRequest request = new PecaCreateRequest("Filtro", "Filtro oleo", new BigDecimal("25.00"), 10);

        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Filtro"));
    }

    @Test
    void getPecasRetorna200() throws Exception {
        when(pecaService.listar()).thenReturn(List.of(criarPeca(1L), criarPeca(2L)));

        mockMvc.perform(get("/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getPecasAtivasRetorna200() throws Exception {
        when(pecaService.listarAtivas()).thenReturn(List.of(criarPeca(1L)));

        mockMvc.perform(get("/pecas/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getPecasEstoqueBaixoRetorna200() throws Exception {
        when(pecaService.listarComEstoqueBaixo(5)).thenReturn(List.of(criarPeca(1L)));

        mockMvc.perform(get("/pecas/estoque-baixo").param("quantidade", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getPecaPorIdRetorna200() throws Exception {
        when(pecaService.buscarPorId(1L)).thenReturn(criarPeca(1L));

        mockMvc.perform(get("/pecas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void putPecaRetorna200() throws Exception {
        when(pecaService.atualizar(eq(1L), any(PecaUpdateRequest.class))).thenReturn(criarPeca(1L));
        PecaUpdateRequest request = new PecaUpdateRequest("Pastilha", "Pastilha freio", new BigDecimal("90.00"), false);

        mockMvc.perform(put("/pecas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patchEstoqueRetorna200() throws Exception {
        when(pecaService.atualizarEstoque(eq(1L), any(AtualizarEstoqueRequest.class))).thenReturn(criarPeca(1L));
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(20);

        mockMvc.perform(patch("/pecas/{id}/estoque", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletePecaRetorna204() throws Exception {
        doNothing().when(pecaService).excluir(1L);

        mockMvc.perform(delete("/pecas/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private Peca criarPeca(Long id) {
        Peca peca = new Peca();
        peca.setId(id);
        peca.setNome("Filtro");
        peca.setDescricao("Filtro oleo");
        peca.setValorUnitario(new BigDecimal("25.00"));
        peca.setQuantidadeEstoque(10);
        peca.setAtivo(true);
        return peca;
    }
}
