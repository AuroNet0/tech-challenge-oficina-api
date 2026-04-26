package com.oficina.api.service;

import com.oficina.api.dto.request.veiculo.VeiculoCreateRequest;
import com.oficina.api.dto.request.veiculo.VeiculoUpdateRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.Veiculo;
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.repository.VeiculoRepository;
import com.oficina.api.validation.PlacaValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Veiculo criar(VeiculoCreateRequest request) {
        validarPlaca(request.placa());

        if (veiculoRepository.existsByPlaca(request.placa())) {
            throw new BusinessException("Placa ja cadastrada.");
        }

        Cliente cliente = buscarClienteOuFalhar(request.clienteId());

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(request.placa());
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        veiculo.setCliente(cliente);

        return veiculoRepository.save(veiculo);
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) {
        return buscarVeiculoOuFalhar(id);
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar() {
        return veiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarPorCliente(Long clienteId) {
        buscarClienteOuFalhar(clienteId);
        return veiculoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Veiculo atualizar(Long id, VeiculoUpdateRequest request) {
        Veiculo veiculo = buscarVeiculoOuFalhar(id);
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public void excluir(Long id) {
        Veiculo veiculo = buscarVeiculoOuFalhar(id);
        veiculoRepository.delete(veiculo);
    }

    private void validarPlaca(String placa) {
        if (!PlacaValidator.isValid(placa)) {
            throw new ValidationException("Placa invalida.");
        }
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }

    private Veiculo buscarVeiculoOuFalhar(Long veiculoId) {
        return veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo nao encontrado."));
    }
}
