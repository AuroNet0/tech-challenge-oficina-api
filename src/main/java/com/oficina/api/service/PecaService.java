package com.oficina.api.service;

import com.oficina.api.dto.request.peca.AtualizarEstoqueRequest;
import com.oficina.api.dto.request.peca.PecaCreateRequest;
import com.oficina.api.dto.request.peca.PecaUpdateRequest;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Peca;
import com.oficina.api.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PecaService {

    private final PecaRepository pecaRepository;

    public PecaService(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public Peca criar(PecaCreateRequest request) {
        validarNome(request.nome());
        validarValorUnitario(request.valorUnitario());
        validarQuantidade(request.quantidadeEstoque());

        Peca peca = new Peca();
        peca.setNome(request.nome());
        peca.setDescricao(request.descricao());
        peca.setValorUnitario(request.valorUnitario());
        peca.setQuantidadeEstoque(request.quantidadeEstoque());
        peca.setAtivo(true);

        return pecaRepository.save(peca);
    }

    @Transactional(readOnly = true)
    public Peca buscarPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public List<Peca> listar() {
        return pecaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Peca> listarAtivas() {
        return pecaRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Peca> listarComEstoqueBaixo(Integer quantidade) {
        return pecaRepository.findByQuantidadeEstoqueLessThanEqual(quantidade);
    }

    @Transactional
    public Peca atualizar(Long id, PecaUpdateRequest request) {
        Peca peca = buscarOuFalhar(id);
        validarNome(request.nome());
        validarValorUnitario(request.valorUnitario());

        peca.setNome(request.nome());
        peca.setDescricao(request.descricao());
        peca.setValorUnitario(request.valorUnitario());
        peca.setAtivo(request.ativo());
        return pecaRepository.save(peca);
    }

    @Transactional
    public Peca atualizarEstoque(Long id, AtualizarEstoqueRequest request) {
        Peca peca = buscarOuFalhar(id);
        validarQuantidade(request.quantidade());
        peca.setQuantidadeEstoque(request.quantidade());
        return pecaRepository.save(peca);
    }

    @Transactional
    public void excluir(Long id) {
        Peca peca = buscarOuFalhar(id);
        pecaRepository.delete(peca);
    }

    private Peca buscarOuFalhar(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Peca nao encontrada."));
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ValidationException("Nome obrigatorio.");
        }
    }

    private void validarValorUnitario(BigDecimal valorUnitario) {
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Valor unitario nao pode ser negativo.");
        }
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade < 0) {
            throw new ValidationException("Quantidade em estoque nao pode ser negativa.");
        }
    }
}
