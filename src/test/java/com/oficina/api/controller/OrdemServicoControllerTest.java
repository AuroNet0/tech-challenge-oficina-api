package com.oficina.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oficina.api.dto.request.ordemservico.AdicionarPecaOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AdicionarServicoOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
import com.oficina.api.dto.request.ordemservico.AtualizarStatusOrdemServicoRequest;
import com.oficina.api.dto.request.ordemservico.CriarOrdemServicoRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.GlobalExceptionHandler;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.ItemPecaOrdem;
import com.oficina.api.model.ItemServicoOrdem;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.Peca;
import com.oficina.api.model.Servico;
import com.oficina.api.model.Veiculo;
import com.oficina.api.model.enums.StatusOrdemServico;
import com.oficina.api.model.enums.TipoPessoa;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private OrdemServicoController ordemServicoController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(ordemServicoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void deveBuscarPorIdComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_DIAGNOSTICO, "170.00");
        when(ordemServicoService.buscarPorId(1L)).thenReturn(ordemServico);

        mockMvc.perform(get("/ordens-servico/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.clienteNome").value("Cliente 1"))
                .andExpect(jsonPath("$.veiculoId").value(10))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1D23"))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.dataAbertura").exists())
                .andExpect(jsonPath("$.valorTotal").value(170.00))
                .andExpect(jsonPath("$.itensServico", hasSize(1)))
                .andExpect(jsonPath("$.itensPeca", hasSize(1)))
                .andExpect(jsonPath("$.orcamento.valorServicos").value(120.00))
                .andExpect(jsonPath("$.orcamento.valorPecas").value(50.00))
                .andExpect(jsonPath("$.orcamento.valorTotal").value(170.00));

        verify(ordemServicoService).buscarPorId(1L);
    }

    @Test
    void deveRetornar404AoBuscarPorIdQuandoNaoEncontrar() throws Exception {
        when(ordemServicoService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Ordem nao encontrada"));

        mockMvc.perform(get("/ordens-servico/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ordem nao encontrada"));
    }

    @Test
    void deveListarOrdensComSucesso() throws Exception {
        OrdemServico os1 = criarOrdemServicoCompleta(1L, StatusOrdemServico.RECEBIDA, "170.00");
        OrdemServico os2 = criarOrdemServicoCompleta(2L, StatusOrdemServico.EM_EXECUCAO, "300.00");
        when(ordemServicoService.listar()).thenReturn(List.of(os1, os2));

        mockMvc.perform(get("/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente 1"))
                .andExpect(jsonPath("$[0].status").value("RECEBIDA"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("EM_EXECUCAO"));
    }

    @Test
    void deveRetornarListaVaziaAoListarQuandoNaoHouverOrdens() throws Exception {
        when(ordemServicoService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveListarOrdensPorClienteComSucesso() throws Exception {
        OrdemServico os = criarOrdemServicoCompleta(1L, StatusOrdemServico.AGUARDANDO_APROVACAO, "170.00");
        when(ordemServicoService.listarPorCliente(1L)).thenReturn(List.of(os));

        mockMvc.perform(get("/ordens-servico/cliente/{clienteId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente 1"));
    }

    @Test
    void deveRetornar404AoListarPorClienteQuandoClienteNaoExistir() throws Exception {
        when(ordemServicoService.listarPorCliente(1L)).thenThrow(new ResourceNotFoundException("Cliente nao encontrado."));

        mockMvc.perform(get("/ordens-servico/cliente/{clienteId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveListarOrdensPorVeiculoComSucesso() throws Exception {
        OrdemServico os = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_EXECUCAO, "170.00");
        when(ordemServicoService.listarPorVeiculo(10L)).thenReturn(List.of(os));

        mockMvc.perform(get("/ordens-servico/veiculo/{veiculoId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].veiculoPlaca").value("ABC1D23"));
    }

    @Test
    void deveRetornar404AoListarPorVeiculoQuandoVeiculoNaoExistir() throws Exception {
        when(ordemServicoService.listarPorVeiculo(10L)).thenThrow(new ResourceNotFoundException("Veiculo nao encontrado."));

        mockMvc.perform(get("/ordens-servico/veiculo/{veiculoId}", 10L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveListarOrdensPorStatusComSucesso() throws Exception {
        OrdemServico os = criarOrdemServicoCompleta(1L, StatusOrdemServico.FINALIZADA, "170.00");
        when(ordemServicoService.listarPorStatus(StatusOrdemServico.FINALIZADA)).thenReturn(List.of(os));

        mockMvc.perform(get("/ordens-servico/status/{status}", "FINALIZADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FINALIZADA"));
    }

    @Test
    void deveRetornar500AoListarPorStatusComValorInvalido() throws Exception {
        mockMvc.perform(get("/ordens-servico/status/{status}", "INVALIDO"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deveCriarOsComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.RECEBIDA, "170.00");
        when(ordemServicoService.criarOrdemServico(org.mockito.ArgumentMatchers.any(CriarOrdemServicoRequest.class)))
                .thenReturn(ordemServico);

        String body = objectMapper.writeValueAsString(new CriarOrdemServicoRequest(1L, 10L, "Obs"));

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void deveRetornar400AoCriarOsComRequestInvalido() throws Exception {
        String body = "{\"observacoes\":\"Obs\"}";

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveAdicionarServicoComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_DIAGNOSTICO, "200.00");
        when(ordemServicoService.adicionarServico(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(AdicionarServicoOrdemRequest.class))).thenReturn(ordemServico);

        String body = objectMapper.writeValueAsString(new AdicionarServicoOrdemRequest(20L, "Obs serv"));

        mockMvc.perform(post("/ordens-servico/{id}/servicos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar400AoAdicionarServicoQuandoBusinessException() throws Exception {
        when(ordemServicoService.adicionarServico(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(AdicionarServicoOrdemRequest.class)))
                .thenThrow(new BusinessException("Transicao de status invalida"));

        String body = objectMapper.writeValueAsString(new AdicionarServicoOrdemRequest(20L, "Obs serv"));

        mockMvc.perform(post("/ordens-servico/{id}/servicos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Transicao de status invalida"));
    }

    @Test
    void deveRetornar400AoAdicionarServicoComBodyInvalido() throws Exception {
        String body = "{\"observacao\":\"Obs\"}";

        mockMvc.perform(post("/ordens-servico/{id}/servicos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveAdicionarPecaComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_DIAGNOSTICO, "200.00");
        when(ordemServicoService.adicionarPeca(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(AdicionarPecaOrdemRequest.class))).thenReturn(ordemServico);

        String body = objectMapper.writeValueAsString(new AdicionarPecaOrdemRequest(30L, 2));

        mockMvc.perform(post("/ordens-servico/{id}/pecas", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar400AoAdicionarPecaComBodyInvalido() throws Exception {
        String body = objectMapper.writeValueAsString(new AdicionarPecaOrdemRequest(30L, 0));

        mockMvc.perform(post("/ordens-servico/{id}/pecas", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveAtualizarStatusComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_EXECUCAO, "200.00");
        when(ordemServicoService.atualizarStatus(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(AtualizarStatusOrdemServicoRequest.class))).thenReturn(ordemServico);

        String body = objectMapper.writeValueAsString(new AtualizarStatusOrdemServicoRequest(StatusOrdemServico.EM_EXECUCAO));

        mockMvc.perform(patch("/ordens-servico/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    void deveRetornar400AoAtualizarStatusComBodyInvalido() throws Exception {
        String body = "{}";

        mockMvc.perform(patch("/ordens-servico/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveAprovarOrcamentoComSucesso() throws Exception {
        OrdemServico ordemServico = criarOrdemServicoCompleta(1L, StatusOrdemServico.EM_EXECUCAO, "200.00");
        when(ordemServicoService.aprovarOrcamento(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(AprovarOrcamentoRequest.class))).thenReturn(ordemServico);

        String body = objectMapper.writeValueAsString(new AprovarOrcamentoRequest(true));

        mockMvc.perform(patch("/ordens-servico/{id}/orcamento", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    void deveRetornar400AoAprovarOrcamentoComBodyInvalido() throws Exception {
        String body = "{}";

        mockMvc.perform(patch("/ordens-servico/{id}/orcamento", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveMapearNullsSemErroNoDetalhamento() throws Exception {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(77L);
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        ordemServico.setDataAbertura(LocalDateTime.now());
        ordemServico.setValorTotal(null);
        ordemServico.setItensServico(null);
        ordemServico.setItensPeca(null);

        when(ordemServicoService.buscarPorId(77L)).thenReturn(ordemServico);

        mockMvc.perform(get("/ordens-servico/{id}", 77L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.clienteId").doesNotExist())
                .andExpect(jsonPath("$.veiculoId").doesNotExist())
                .andExpect(jsonPath("$.valorTotal").value(0))
                .andExpect(jsonPath("$.itensServico", hasSize(0)))
                .andExpect(jsonPath("$.itensPeca", hasSize(0)))
                .andExpect(jsonPath("$.orcamento.valorTotal").value(0));
    }

    private OrdemServico criarOrdemServicoCompleta(Long id, StatusOrdemServico status, String valorTotal) {
        Cliente cliente = criarCliente(1L);
        Veiculo veiculo = criarVeiculo(10L, cliente);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(id);
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setStatus(status);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 4, 25, 10, 0));
        ordemServico.setValorTotal(new BigDecimal(valorTotal));
        ordemServico.setObservacoes("Observacao teste");
        ordemServico.setItensServico(new ArrayList<>());
        ordemServico.setItensPeca(new ArrayList<>());

        Servico servico = criarServico(20L, "Troca oleo", "120.00");
        Peca peca = criarPeca(30L, "Filtro", "25.00", 10);

        ItemServicoOrdem itemServico = new ItemServicoOrdem();
        itemServico.setId(1000L);
        itemServico.setOrdemServico(ordemServico);
        itemServico.setServico(servico);
        itemServico.setValor(new BigDecimal("120.00"));
        itemServico.setObservacao("Item servico");

        ItemPecaOrdem itemPeca = new ItemPecaOrdem();
        itemPeca.setId(2000L);
        itemPeca.setOrdemServico(ordemServico);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(2);
        itemPeca.setValorUnitario(new BigDecimal("25.00"));

        ordemServico.getItensServico().add(itemServico);
        ordemServico.getItensPeca().add(itemPeca);
        return ordemServico;
    }

    private Cliente criarCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente " + id);
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setCpfCnpj("1234567890" + id);
        cliente.setTelefone("11999999999");
        return cliente;
    }

    private Veiculo criarVeiculo(Long id, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setCliente(cliente);
        veiculo.setPlaca("ABC1D23");
        veiculo.setMarca("Marca");
        veiculo.setModelo("Modelo");
        veiculo.setAno(2020);
        return veiculo;
    }

    private Servico criarServico(Long id, String descricao, String valor) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setDescricao(descricao);
        servico.setValor(new BigDecimal(valor));
        servico.setTempoEstimadoMinutos(60);
        servico.setAtivo(true);
        return servico;
    }

    private Peca criarPeca(Long id, String nome, String valorUnitario, Integer estoque) {
        Peca peca = new Peca();
        peca.setId(id);
        peca.setNome(nome);
        peca.setDescricao("Descricao " + nome);
        peca.setValorUnitario(new BigDecimal(valorUnitario));
        peca.setQuantidadeEstoque(estoque);
        peca.setAtivo(true);
        return peca;
    }
}
