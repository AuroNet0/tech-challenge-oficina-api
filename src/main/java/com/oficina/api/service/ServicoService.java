package com.oficina.api.service;

import com.oficina.api.dto.request.servico.ServicoCreateRequest;
import com.oficina.api.dto.request.servico.ServicoUpdateRequest;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Servico;
import com.oficina.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public Servico criar(ServicoCreateRequest request) {
        validarDados(request.descricao(), request.valor(), request.tempoEstimadoMinutos());

        Servico servico = new Servico();
        servico.setDescricao(request.descricao());
        servico.setValor(request.valor());
        servico.setTempoEstimadoMinutos(request.tempoEstimadoMinutos());
        servico.setAtivo(true);
        return servicoRepository.save(servico);
    }

    @Transactional(readOnly = true)
    public Servico buscarPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() {
        return servicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Servico> listarAtivos() {
        return servicoRepository.findByAtivoTrue();
    }

    @Transactional
    public Servico atualizar(Long id, ServicoUpdateRequest request) {
        Servico servico = buscarOuFalhar(id);
        validarDados(request.descricao(), request.valor(), request.tempoEstimadoMinutos());

        servico.setDescricao(request.descricao());
        servico.setValor(request.valor());
        servico.setTempoEstimadoMinutos(request.tempoEstimadoMinutos());
        servico.setAtivo(request.ativo());
        return servicoRepository.save(servico);
    }

    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarOuFalhar(id);
        servicoRepository.delete(servico);
    }

    private Servico buscarOuFalhar(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado."));
    }

    private void validarDados(String descricao, BigDecimal valor, Integer tempoEstimadoMinutos) {
        if (descricao == null || descricao.isBlank()) {
            throw new ValidationException("Descricao obrigatoria.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Valor nao pode ser negativo.");
        }
        if (tempoEstimadoMinutos == null || tempoEstimadoMinutos <= 0) {
            throw new ValidationException("Tempo estimado deve ser maior que zero.");
        }
    }
}
