package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.veiculo.VeiculoCreateRequest;
import com.oficina.api.dto.request.veiculo.VeiculoUpdateRequest;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.Veiculo;
import com.oficina.api.model.enums.TipoPessoa;
import com.oficina.api.service.VeiculoService;
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
class VeiculoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private VeiculoService veiculoService;

    @InjectMocks
    private VeiculoController veiculoController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(veiculoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postVeiculosDeveRetornar201() throws Exception {
        Veiculo veiculo = criarVeiculo(10L, criarCliente(1L));
        when(veiculoService.criar(any(VeiculoCreateRequest.class))).thenReturn(veiculo);

        VeiculoCreateRequest request = new VeiculoCreateRequest("ABC1D23", "VW", "Gol", 2020, 1L);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    @Test
    void getVeiculosDeveRetornar200() throws Exception {
        when(veiculoService.listar()).thenReturn(List.of(
                criarVeiculo(10L, criarCliente(1L)),
                criarVeiculo(11L, criarCliente(2L))
        ));

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getVeiculoPorIdDeveRetornar200() throws Exception {
        when(veiculoService.buscarPorId(10L)).thenReturn(criarVeiculo(10L, criarCliente(1L)));

        mockMvc.perform(get("/veiculos/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    @Test
    void getVeiculosPorClienteDeveRetornar200() throws Exception {
        Cliente cliente = criarCliente(1L);
        when(veiculoService.listarPorCliente(1L)).thenReturn(List.of(
                criarVeiculo(10L, cliente),
                criarVeiculo(11L, cliente)
        ));

        mockMvc.perform(get("/veiculos/cliente/{clienteId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clienteId").value(1));
    }

    @Test
    void putVeiculoDeveRetornar200() throws Exception {
        Veiculo veiculo = criarVeiculo(10L, criarCliente(1L));
        when(veiculoService.atualizar(eq(10L), any(VeiculoUpdateRequest.class))).thenReturn(veiculo);

        VeiculoUpdateRequest request = new VeiculoUpdateRequest("Fiat", "Argo", 2022);

        mockMvc.perform(put("/veiculos/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    @Test
    void deleteVeiculoDeveRetornar204() throws Exception {
        doNothing().when(veiculoService).excluir(10L);

        mockMvc.perform(delete("/veiculos/{id}", 10L))
                .andExpect(status().isNoContent());
    }

    private Cliente criarCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente " + id);
        cliente.setCpfCnpj("111.444.777-35");
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setTelefone("11999999999");
        return cliente;
    }

    private Veiculo criarVeiculo(Long id, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setPlaca("ABC1D23");
        veiculo.setMarca("VW");
        veiculo.setModelo("Gol");
        veiculo.setAno(2020);
        veiculo.setCliente(cliente);
        return veiculo;
    }
}
