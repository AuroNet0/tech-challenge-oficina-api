package com.oficina.api.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoTest {

    @Test
    void prePersistDeveAplicarDefaultQuandoAtivoForNulo() {
        Servico servico = new Servico();
        servico.setAtivo(null);

        servico.prePersist();

        assertThat(servico.getAtivo()).isTrue();
    }

    @Test
    void prePersistNaoDeveSobrescreverAtivoQuandoJaPreenchido() {
        Servico servico = new Servico();
        servico.setAtivo(false);

        servico.prePersist();

        assertThat(servico.getAtivo()).isFalse();
    }

    @Test
    void equalsHashCodeEConstrutoresDevemFuncionarCorretamente() {
        Servico s1 = new Servico();
        Servico s2 = new Servico();
        s1.setId(1L);
        s2.setId(1L);
        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        assertThat(s1.equals(null)).isFalse();
        assertThat(s1.equals("x")).isFalse();

        List<ItemServicoOrdem> itens = new ArrayList<>();
        Servico completo = new Servico(10L, "Alinhamento", new BigDecimal("120.00"), 60, true, itens);
        assertThat(completo.getId()).isEqualTo(10L);
        assertThat(completo.getDescricao()).isEqualTo("Alinhamento");
        assertThat(completo.getValor()).isEqualByComparingTo("120.00");
        assertThat(completo.getTempoEstimadoMinutos()).isEqualTo(60);
        assertThat(completo.getAtivo()).isTrue();
        assertThat(completo.getItensOrdem()).isSameAs(itens);
    }

    @Test
    void construtorVazioEGettersSettersDevemFuncionar() {
        Servico servico = new Servico();
        assertThat(servico.getAtivo()).isTrue();
        assertThat(servico.getItensOrdem()).isNotNull();

        List<ItemServicoOrdem> itens = new ArrayList<>();
        servico.setId(2L);
        servico.setDescricao("Balanceamento");
        servico.setValor(new BigDecimal("80.00"));
        servico.setTempoEstimadoMinutos(45);
        servico.setAtivo(false);
        servico.setItensOrdem(itens);

        assertThat(servico.getId()).isEqualTo(2L);
        assertThat(servico.getDescricao()).isEqualTo("Balanceamento");
        assertThat(servico.getValor()).isEqualByComparingTo("80.00");
        assertThat(servico.getTempoEstimadoMinutos()).isEqualTo(45);
        assertThat(servico.getAtivo()).isFalse();
        assertThat(servico.getItensOrdem()).isSameAs(itens);
    }
}
