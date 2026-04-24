package com.oficina.api.repository;

import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByClienteId(Long clienteId);

    List<OrdemServico> findByVeiculoId(Long veiculoId);

    List<OrdemServico> findByStatus(StatusOrdemServico status);
}
