package com.oficina.api.repository;

import com.oficina.api.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PecaRepository extends JpaRepository<Peca, Long> {

    List<Peca> findByAtivoTrue();

    List<Peca> findByQuantidadeEstoqueLessThanEqual(Integer quantidade);
}
