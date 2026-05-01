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
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.repository.OrdemServicoRepository;
import com.oficina.api.repository.PecaRepository;
import com.oficina.api.repository.ServicoRepository;
import com.oficina.api.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaRepository pecaRepository;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository,
                               ServicoRepository servicoRepository,
                               PecaRepository pecaRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public OrdemServico criarOrdemServico(CriarOrdemServicoRequest request) {
        Cliente cliente = buscarClienteOuFalhar(request.clienteId());
        Veiculo veiculo = buscarVeiculoOuFalhar(request.veiculoId());

        validarVeiculoDoCliente(cliente, veiculo);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setObservacoes(request.observacoes());
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        ordemServico.setDataAbertura(LocalDateTime.now());
        ordemServico.setValorTotal(BigDecimal.ZERO);

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServico adicionarServico(Long ordemServicoId, AdicionarServicoOrdemRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoOuFalhar(ordemServicoId);
        garantirStatusEmDiagnostico(ordemServico);
        Servico servico = buscarServicoOuFalhar(request.servicoId());

        ItemServicoOrdem itemServico = new ItemServicoOrdem();
        itemServico.setOrdemServico(ordemServico);
        itemServico.setServico(servico);
        itemServico.setValor(servico.getValor());
        itemServico.setObservacao(request.observacao());

        ordemServico.getItensServico().add(itemServico);
        recalcularValorTotal(ordemServico);

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServico adicionarPeca(Long ordemServicoId, AdicionarPecaOrdemRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoOuFalhar(ordemServicoId);
        garantirStatusEmDiagnostico(ordemServico);
        Peca peca = buscarPecaOuFalhar(request.pecaId());

        validarEstoqueDisponivel(peca, request.quantidade());

        ItemPecaOrdem itemPeca = new ItemPecaOrdem();
        itemPeca.setOrdemServico(ordemServico);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(request.quantidade());
        itemPeca.setValorUnitario(peca.getValorUnitario());

        ordemServico.getItensPeca().add(itemPeca);
        recalcularValorTotal(ordemServico);

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServico atualizarStatus(Long ordemServicoId, AtualizarStatusOrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoOuFalhar(ordemServicoId);
        StatusOrdemServico statusAtual = ordemServico.getStatus();
        StatusOrdemServico novoStatus = request.status();

        validarTransicaoStatus(statusAtual, novoStatus);
        ordemServico.setStatus(novoStatus);

        if (novoStatus == StatusOrdemServico.FINALIZADA) {
            ordemServico.setDataFinalizacao(LocalDateTime.now());
        }

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServico aprovarOrcamento(Long ordemServicoId, AprovarOrcamentoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoOuFalhar(ordemServicoId);
        StatusOrdemServico statusAtual = ordemServico.getStatus();

        if (statusAtual != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("A ordem de servico deve estar AGUARDANDO_APROVACAO para aprovacao.");
        }

        if (Boolean.TRUE.equals(request.aprovado())) {
            validarTransicaoStatus(statusAtual, StatusOrdemServico.EM_EXECUCAO);
            //TO-DO: Lógica de baixa no estoque.
            ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
            if (ordemServico.getDataInicioExecucao() == null) {
                ordemServico.setDataInicioExecucao(LocalDateTime.now());
            }
        } else {
            ordemServico.setStatus(StatusOrdemServico.CANCELADA);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServico enviarOrcamento(Long ordemServicoId) {
        OrdemServico ordemServico = buscarOrdemServicoOuFalhar(ordemServicoId);

        if (ordemServico.getStatus() != StatusOrdemServico.EM_DIAGNOSTICO) {
            throw new BusinessException("A ordem de servico deve estar EM_DIAGNOSTICO para envio de orcamento.");
        }

        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarPorId(Long id) {
        return buscarOrdemServicoOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listar() {
        return ordemServicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorCliente(Long clienteId) {
        buscarClienteOuFalhar(clienteId);
        return ordemServicoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorVeiculo(Long veiculoId) {
        buscarVeiculoOuFalhar(veiculoId);
        return ordemServicoRepository.findByVeiculoId(veiculoId);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorStatus(StatusOrdemServico status) {
        return ordemServicoRepository.findByStatus(status);
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }

    private Veiculo buscarVeiculoOuFalhar(Long veiculoId) {
        return veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo nao encontrado."));
    }

    private OrdemServico buscarOrdemServicoOuFalhar(Long ordemServicoId) {
        return ordemServicoRepository.findById(ordemServicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de servico nao encontrada."));
    }

    private Servico buscarServicoOuFalhar(Long servicoId) {
        return servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado."));
    }

    private Peca buscarPecaOuFalhar(Long pecaId) {
        return pecaRepository.findById(pecaId)
                .orElseThrow(() -> new ResourceNotFoundException("Peca nao encontrada."));
    }

    private void validarVeiculoDoCliente(Cliente cliente, Veiculo veiculo) {
        if (veiculo.getCliente() == null || !cliente.getId().equals(veiculo.getCliente().getId())) {
            throw new BusinessException("O veiculo informado nao pertence ao cliente.");
        }
    }

    private void validarEstoqueDisponivel(Peca peca, Integer quantidadeSolicitada) {
        Integer estoqueAtual = peca.getQuantidadeEstoque() == null ? 0 : peca.getQuantidadeEstoque();
        if (estoqueAtual < quantidadeSolicitada) {
            throw new BusinessException("Estoque insuficiente para a peca informada.");
        }
    }

    private void validarTransicaoStatus(StatusOrdemServico statusAtual, StatusOrdemServico novoStatus) {
        boolean transicaoValida =
                (statusAtual == StatusOrdemServico.RECEBIDA && novoStatus == StatusOrdemServico.EM_DIAGNOSTICO)
                        || (statusAtual == StatusOrdemServico.EM_DIAGNOSTICO && novoStatus == StatusOrdemServico.AGUARDANDO_APROVACAO)
                        || (statusAtual == StatusOrdemServico.AGUARDANDO_APROVACAO && novoStatus == StatusOrdemServico.EM_EXECUCAO)
                        || (statusAtual == StatusOrdemServico.EM_EXECUCAO && novoStatus == StatusOrdemServico.FINALIZADA)
                        || (statusAtual == StatusOrdemServico.FINALIZADA && novoStatus == StatusOrdemServico.ENTREGUE);

        if (!transicaoValida) {
            throw new BusinessException("Transicao de status invalida de " + statusAtual + " para " + novoStatus);
        }
    }

    private void recalcularValorTotal(OrdemServico ordemServico) {
        BigDecimal totalServicos = (ordemServico.getItensServico() == null ? Collections.<ItemServicoOrdem>emptyList() : ordemServico.getItensServico()).stream()
                .map(item -> item.getValor() == null ? BigDecimal.ZERO : item.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPecas = (ordemServico.getItensPeca() == null ? Collections.<ItemPecaOrdem>emptyList() : ordemServico.getItensPeca()).stream()
                .map(item -> {
                    BigDecimal valorUnitario = item.getValorUnitario() == null ? BigDecimal.ZERO : item.getValorUnitario();
                    Integer quantidade = item.getQuantidade() == null ? 0 : item.getQuantidade();
                    return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ordemServico.setValorTotal(totalServicos.add(totalPecas));
    }

    private void garantirStatusEmDiagnostico(OrdemServico ordemServico) {
        if (ordemServico.getStatus() != StatusOrdemServico.EM_DIAGNOSTICO) {
            ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        }
    }
}
