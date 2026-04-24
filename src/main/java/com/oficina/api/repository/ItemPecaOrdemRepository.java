package com.oficina.api.repository;

import com.oficina.api.model.ItemPecaOrdem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemPecaOrdemRepository extends JpaRepository<ItemPecaOrdem, Long> {

    List<ItemPecaOrdem> findByOrdemServicoId(Long ordemServicoId);
}
