package com.oficina.api.service;

import com.oficina.api.dto.request.cliente.ClienteCreateRequest;
import com.oficina.api.dto.request.cliente.ClienteUpdateRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Cliente;
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.validation.CpfCnpjValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente criar(ClienteCreateRequest request) {
        validarCpfCnpj(request.cpfCnpj());

        if (clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new BusinessException("CPF/CNPJ ja cadastrado.");
        }

        if (request.email() != null && !request.email().isBlank()
                && clienteRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email ja cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTipoPessoa(request.tipoPessoa());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCpfOuCnpj(String cpfCnpj) {
        validarCpfCnpj(cpfCnpj);
        return clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = buscarOuFalhar(id);

        if (request.email() != null && !request.email().isBlank()
                && !request.email().equals(cliente.getEmail())
                && clienteRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email ja cadastrado.");
        }

        cliente.setNome(request.nome());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarOuFalhar(id);
        clienteRepository.delete(cliente);
    }

    private void validarCpfCnpj(String cpfCnpj) {
        if (!CpfCnpjValidator.isValid(cpfCnpj)) {
            throw new ValidationException("CPF/CNPJ invalido.");
        }
    }

    private Cliente buscarOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }
}
