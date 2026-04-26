package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.cliente.ClienteCreateRequest;
import com.oficina.api.dto.request.cliente.ClienteUpdateRequest;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.enums.TipoPessoa;
import com.oficina.api.service.ClienteService;
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

import java.time.LocalDateTime;
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
class ClienteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postClientesDeveRetornar201() throws Exception {
        Cliente cliente = criarCliente(1L);
        when(clienteService.criar(any(ClienteCreateRequest.class))).thenReturn(cliente);

        ClienteCreateRequest request = new ClienteCreateRequest(
                "Joao",
                "111.444.777-35",
                TipoPessoa.PF,
                "11999999999",
                "joao@email.com"
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Joao"));
    }

    @Test
    void getClientesDeveRetornar200() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(criarCliente(1L), criarCliente(2L)));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getClientePorIdDeveRetornar200() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(criarCliente(1L));

        mockMvc.perform(get("/clientes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpfCnpj").value("111.444.777-35"));
    }

    @Test
    void putClienteDeveRetornar200() throws Exception {
        when(clienteService.atualizar(eq(1L), any(ClienteUpdateRequest.class))).thenReturn(criarCliente(1L));

        ClienteUpdateRequest request = new ClienteUpdateRequest(
                "Joao Atualizado",
                "11888887777",
                "joao@email.com"
        );

        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Joao"));
    }

    @Test
    void deleteClienteDeveRetornar204() throws Exception {
        doNothing().when(clienteService).excluir(1L);

        mockMvc.perform(delete("/clientes/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private Cliente criarCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Joao");
        cliente.setCpfCnpj("111.444.777-35");
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setTelefone("11999999999");
        cliente.setEmail("joao@email.com");
        cliente.setDataCadastro(LocalDateTime.of(2026, 4, 25, 10, 0));
        return cliente;
    }
}
