package com.oficina.api.repository;

import com.oficina.api.model.ItemServicoOrdem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemServicoOrdemRepository extends JpaRepository<ItemServicoOrdem, Long> {

    List<ItemServicoOrdem> findByOrdemServicoId(Long ordemServicoId);
}
