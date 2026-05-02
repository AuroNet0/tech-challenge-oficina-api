package com.oficina.api.service;

import com.oficina.api.dto.request.ordemservico.AdicionarPecaOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AdicionarServicoOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
import com.oficina.api.dto.request.ordemservico.AtualizarStatusOrdemServicoRequest;
import com.oficina.api.dto.request.ordemservico.CriarOrdemServicoRequest;
import com.oficina.api.exception.BusinessException;
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
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.repository.OrdemServicoRepository;
import com.oficina.api.repository.PecaRepository;
import com.oficina.api.repository.ServicoRepository;
import com.oficina.api.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PecaRepository pecaRepository;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    @BeforeEach
    void setUp() {
        lenient().when(ordemServicoRepository.save(any(OrdemServico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarOsComSucesso() {
        Cliente cliente = criarCliente(1L);
        Veiculo veiculo = criarVeiculo(10L, cliente);
        CriarOrdemServicoRequest request = new CriarOrdemServicoRequest(1L, 10L, "Troca de oleo");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        OrdemServico resultado = ordemServicoService.criarOrdemServico(request);

        assertThat(resultado.getCliente()).isEqualTo(cliente);
        assertThat(resultado.getVeiculo()).isEqualTo(veiculo);
        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(resultado.getDataAbertura()).isNotNull();
        assertThat(resultado.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getObservacoes()).isEqualTo("Troca de oleo");

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoClienteNaoExistirAoCriarOs() {
        CriarOrdemServicoRequest request = new CriarOrdemServicoRequest(1L, 10L, "Obs");
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.criarOrdemServico(request));

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoVeiculoNaoExistirAoCriarOs() {
        Cliente cliente = criarCliente(1L);
        CriarOrdemServicoRequest request = new CriarOrdemServicoRequest(1L, 10L, "Obs");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.criarOrdemServico(request));

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoVeiculoNaoPertencerAoClienteAoCriarOs() {
        Cliente cliente = criarCliente(1L);
        Cliente outroCliente = criarCliente(2L);
        Veiculo veiculo = criarVeiculo(10L, outroCliente);
        CriarOrdemServicoRequest request = new CriarOrdemServicoRequest(1L, 10L, "Obs");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        assertThrows(BusinessException.class, () -> ordemServicoService.criarOrdemServico(request));

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveAdicionarServicoAOsComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        ordemServico.getItensServico().add(criarItemServico(ordemServico, criarServico(30L, "Diagnostico", "50.00"), "base"));
        ordemServico.getItensPeca().add(criarItemPeca(ordemServico, criarPeca(40L, "Filtro", "10.00", 5), 2));

        Servico servico = criarServico(20L, "Alinhamento", "80.00");
        AdicionarServicoOrdemRequest request = new AdicionarServicoOrdemRequest(20L, "Executar com prioridade");

        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));
        when(servicoRepository.findById(20L)).thenReturn(Optional.of(servico));

        OrdemServico resultado = ordemServicoService.adicionarServico(100L, request);

        assertThat(resultado.getItensServico()).hasSize(2);
        ItemServicoOrdem novoItem = resultado.getItensServico().get(1);
        assertThat(novoItem.getServico()).isEqualTo(servico);
        assertThat(novoItem.getValor()).isEqualByComparingTo("80.00");
        assertThat(novoItem.getObservacao()).isEqualTo("Executar com prioridade");
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("150.00");

        verify(ordemServicoRepository).findById(100L);
        verify(servicoRepository).findById(20L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoOsNaoExistirAoAdicionarServico() {
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordemServicoService.adicionarServico(100L, new AdicionarServicoOrdemRequest(20L, "Obs")));

        verify(ordemServicoRepository).findById(100L);
        verify(servicoRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoServicoNaoExistirAoAdicionarServico() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));
        when(servicoRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordemServicoService.adicionarServico(100L, new AdicionarServicoOrdemRequest(20L, "Obs")));

        verify(ordemServicoRepository).findById(100L);
        verify(servicoRepository).findById(20L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoStatusNaoPermitirAdicionarServico() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.EM_EXECUCAO);

        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        assertThrows(BusinessException.class,
                () -> ordemServicoService.adicionarServico(100L, new AdicionarServicoOrdemRequest(20L, "Obs")));

        verify(ordemServicoRepository).findById(100L);
        verify(servicoRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveAdicionarPecaAOsComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);
        ordemServico.getItensServico().add(criarItemServico(ordemServico, criarServico(30L, "Diagnostico", "30.00"), "base"));
        ordemServico.getItensPeca().add(criarItemPeca(ordemServico, criarPeca(40L, "Filtro", "10.00", 10), 2));

        Peca peca = criarPeca(50L, "Pastilha", "15.00", 10);
        AdicionarPecaOrdemRequest request = new AdicionarPecaOrdemRequest(50L, 3);

        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));
        when(pecaRepository.findById(50L)).thenReturn(Optional.of(peca));

        OrdemServico resultado = ordemServicoService.adicionarPeca(100L, request);

        assertThat(resultado.getItensPeca()).hasSize(2);
        ItemPecaOrdem novoItem = resultado.getItensPeca().get(1);
        assertThat(novoItem.getPeca()).isEqualTo(peca);
        assertThat(novoItem.getQuantidade()).isEqualTo(3);
        assertThat(novoItem.getValorUnitario()).isEqualByComparingTo("15.00");
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("95.00");
        assertThat(peca.getQuantidadeEstoque()).isEqualTo(10);

        verify(ordemServicoRepository).findById(100L);
        verify(pecaRepository).findById(50L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoOsNaoExistirAoAdicionarPeca() {
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordemServicoService.adicionarPeca(100L, new AdicionarPecaOrdemRequest(50L, 2)));

        verify(ordemServicoRepository).findById(100L);
        verify(pecaRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoPecaNaoExistirAoAdicionarPeca() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));
        when(pecaRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordemServicoService.adicionarPeca(100L, new AdicionarPecaOrdemRequest(50L, 2)));

        verify(ordemServicoRepository).findById(100L);
        verify(pecaRepository).findById(50L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoEstoqueInsuficienteAoAdicionarPeca() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        Peca peca = criarPeca(50L, "Pastilha", "15.00", 1);

        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));
        when(pecaRepository.findById(50L)).thenReturn(Optional.of(peca));

        assertThrows(BusinessException.class,
                () -> ordemServicoService.adicionarPeca(100L, new AdicionarPecaOrdemRequest(50L, 2)));

        verify(ordemServicoRepository).findById(100L);
        verify(pecaRepository).findById(50L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoStatusNaoPermitirAdicionarPeca() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.FINALIZADA);

        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        assertThrows(BusinessException.class,
                () -> ordemServicoService.adicionarPeca(100L, new AdicionarPecaOrdemRequest(50L, 2)));

        verify(ordemServicoRepository).findById(100L);
        verify(pecaRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveAtualizarStatusDeRecebidaParaEmDiagnostico() {
        deveAtualizarStatusComSucesso(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void deveAtualizarStatusDeEmDiagnosticoParaAguardandoAprovacao() {
        deveAtualizarStatusComSucesso(StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void deveAtualizarStatusDeAguardandoAprovacaoParaEmExecucao() {
        deveAtualizarStatusComSucesso(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    void deveAtualizarStatusDeEmExecucaoParaFinalizadaEPreencherDataFinalizacao() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.EM_EXECUCAO);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.atualizarStatus(
                100L, new AtualizarStatusOrdemServicoRequest(StatusOrdemServico.FINALIZADA));

        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(resultado.getDataFinalizacao()).isNotNull();

        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveAtualizarStatusDeFinalizadaParaEntregue() {
        deveAtualizarStatusComSucesso(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
    }

    @Test
    void deveLancarBusinessExceptionParaTransicaoInvalidaAoAtualizarStatus() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        BusinessException exception = assertThrows(BusinessException.class, () -> ordemServicoService.atualizarStatus(
                100L, new AtualizarStatusOrdemServicoRequest(StatusOrdemServico.FINALIZADA)));

        assertThat(exception.getMessage()).contains("RECEBIDA").contains("FINALIZADA");
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoOsNaoExistirAoAtualizarStatus() {
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.atualizarStatus(
                100L, new AtualizarStatusOrdemServicoRequest(StatusOrdemServico.EM_DIAGNOSTICO)));

        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveAprovarOrcamentoQuandoStatusForAguardandoAprovacao() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(true));

        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveBaixarEstoqueDasPecasAoAprovarOrcamento() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        Peca peca = criarPeca(50L, "Pastilha", "15.00", 10);
        ordemServico.getItensPeca().add(criarItemPeca(ordemServico, peca, 3));
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(true));

        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(peca.getQuantidadeEstoque()).isEqualTo(7);
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoEstoqueInsuficienteAoAprovarOrcamento() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        Peca peca = criarPeca(50L, "Pastilha", "15.00", 1);
        ordemServico.getItensPeca().add(criarItemPeca(ordemServico, peca, 2));
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        assertThrows(BusinessException.class,
                () -> ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(true)));

        assertThat(peca.getQuantidadeEstoque()).isEqualTo(1);
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveReprovarOrcamentoQuandoStatusForAguardandoAprovacao() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(false));

        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarBusinessExceptionQuandoOsNaoEstiverAguardandoAprovacaoAoAprovarOrcamento() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        assertThrows(BusinessException.class,
                () -> ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(true)));

        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoOsNaoExistirAoAprovarOrcamento() {
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordemServicoService.aprovarOrcamento(100L, new AprovarOrcamentoRequest(true)));

        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveRetornarOsQuandoBuscarPorIdExistente() {
        OrdemServico ordemServico = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.buscarPorId(100L);

        assertThat(resultado).isEqualTo(ordemServico);
        verify(ordemServicoRepository).findById(100L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoBuscarPorIdInexistente() {
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.buscarPorId(100L));

        verify(ordemServicoRepository).findById(100L);
    }

    @Test
    void deveRetornarTodasAsOsAoListar() {
        List<OrdemServico> ordens = List.of(
                criarOrdemServico(100L, StatusOrdemServico.RECEBIDA),
                criarOrdemServico(101L, StatusOrdemServico.EM_DIAGNOSTICO)
        );
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        List<OrdemServico> resultado = ordemServicoService.listar();

        assertThat(resultado).hasSize(2).containsExactlyElementsOf(ordens);
        verify(ordemServicoRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaAoListarQuandoNaoHouverOs() {
        when(ordemServicoRepository.findAll()).thenReturn(List.of());

        List<OrdemServico> resultado = ordemServicoService.listar();

        assertThat(resultado).isEmpty();
        verify(ordemServicoRepository).findAll();
    }

    @Test
    void deveRetornarOsDoClienteAoListarPorCliente() {
        Cliente cliente = criarCliente(1L);
        List<OrdemServico> ordens = List.of(
                criarOrdemServico(100L, StatusOrdemServico.RECEBIDA),
                criarOrdemServico(101L, StatusOrdemServico.EM_DIAGNOSTICO)
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ordemServicoRepository.findByClienteId(1L)).thenReturn(ordens);

        List<OrdemServico> resultado = ordemServicoService.listarPorCliente(1L);

        assertThat(resultado).hasSize(2).containsExactlyElementsOf(ordens);
        verify(clienteRepository).findById(1L);
        verify(ordemServicoRepository).findByClienteId(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoClienteNaoExistirAoListarPorCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.listarPorCliente(1L));

        verify(clienteRepository).findById(1L);
        verify(ordemServicoRepository, never()).findByClienteId(any());
    }

    @Test
    void deveRetornarOsDoVeiculoAoListarPorVeiculo() {
        Cliente cliente = criarCliente(1L);
        Veiculo veiculo = criarVeiculo(10L, cliente);
        List<OrdemServico> ordens = List.of(
                criarOrdemServico(100L, StatusOrdemServico.RECEBIDA),
                criarOrdemServico(101L, StatusOrdemServico.AGUARDANDO_APROVACAO)
        );

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(ordemServicoRepository.findByVeiculoId(10L)).thenReturn(ordens);

        List<OrdemServico> resultado = ordemServicoService.listarPorVeiculo(10L);

        assertThat(resultado).hasSize(2).containsExactlyElementsOf(ordens);
        verify(veiculoRepository).findById(10L);
        verify(ordemServicoRepository).findByVeiculoId(10L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoVeiculoNaoExistirAoListarPorVeiculo() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.listarPorVeiculo(10L));

        verify(veiculoRepository).findById(10L);
        verify(ordemServicoRepository, never()).findByVeiculoId(any());
    }

    @Test
    void deveRetornarOsPorStatusAoListarPorStatus() {
        List<OrdemServico> ordens = List.of(
                criarOrdemServico(100L, StatusOrdemServico.EM_EXECUCAO),
                criarOrdemServico(101L, StatusOrdemServico.EM_EXECUCAO)
        );
        when(ordemServicoRepository.findByStatus(StatusOrdemServico.EM_EXECUCAO)).thenReturn(ordens);

        List<OrdemServico> resultado = ordemServicoService.listarPorStatus(StatusOrdemServico.EM_EXECUCAO);

        assertThat(resultado).hasSize(2).containsExactlyElementsOf(ordens);
        verify(ordemServicoRepository).findByStatus(StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverOsComStatus() {
        when(ordemServicoRepository.findByStatus(StatusOrdemServico.CANCELADA)).thenReturn(List.of());

        List<OrdemServico> resultado = ordemServicoService.listarPorStatus(StatusOrdemServico.CANCELADA);

        assertThat(resultado).isEmpty();
        verify(ordemServicoRepository).findByStatus(StatusOrdemServico.CANCELADA);
    }

    private void deveAtualizarStatusComSucesso(StatusOrdemServico statusAtual, StatusOrdemServico novoStatus) {
        OrdemServico ordemServico = criarOrdemServico(100L, statusAtual);
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.atualizarStatus(
                100L, new AtualizarStatusOrdemServicoRequest(novoStatus));

        assertThat(resultado.getStatus()).isEqualTo(novoStatus);
        verify(ordemServicoRepository).findById(100L);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
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

    private OrdemServico criarOrdemServico(Long id, StatusOrdemServico status) {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(id);
        ordemServico.setCliente(criarCliente(1L));
        ordemServico.setVeiculo(criarVeiculo(10L, ordemServico.getCliente()));
        ordemServico.setStatus(status);
        ordemServico.setValorTotal(BigDecimal.ZERO);
        ordemServico.setItensServico(new ArrayList<>());
        ordemServico.setItensPeca(new ArrayList<>());
        return ordemServico;
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

    private ItemServicoOrdem criarItemServico(OrdemServico ordemServico, Servico servico, String observacao) {
        ItemServicoOrdem item = new ItemServicoOrdem();
        item.setOrdemServico(ordemServico);
        item.setServico(servico);
        item.setValor(servico.getValor());
        item.setObservacao(observacao);
        return item;
    }

    private ItemPecaOrdem criarItemPeca(OrdemServico ordemServico, Peca peca, Integer quantidade) {
        ItemPecaOrdem item = new ItemPecaOrdem();
        item.setOrdemServico(ordemServico);
        item.setPeca(peca);
        item.setQuantidade(quantidade);
        item.setValorUnitario(peca.getValorUnitario());
        return item;
    }
}
