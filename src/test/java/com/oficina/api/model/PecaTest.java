package com.oficina.api.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PecaTest {

    @Test
    void prePersistDeveAplicarDefaultsQuandoCamposForemNulos() {
        Peca peca = new Peca();
        peca.setQuantidadeEstoque(null);
        peca.setAtivo(null);

        peca.prePersist();

        assertThat(peca.getQuantidadeEstoque()).isEqualTo(0);
        assertThat(peca.getAtivo()).isTrue();
    }

    @Test
    void prePersistNaoDeveSobrescreverCamposQuandoJaPreenchidos() {
        Peca peca = new Peca();
        peca.setQuantidadeEstoque(15);
        peca.setAtivo(false);

        peca.prePersist();

        assertThat(peca.getQuantidadeEstoque()).isEqualTo(15);
        assertThat(peca.getAtivo()).isFalse();
    }

    @Test
    void equalsHashCodeEConstrutoresDevemFuncionarCorretamente() {
        Peca p1 = new Peca();
        Peca p2 = new Peca();
        p1.setId(1L);
        p2.setId(1L);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        assertThat(p1.equals(null)).isFalse();
        assertThat(p1.equals("x")).isFalse();

        List<ItemPecaOrdem> itens = new ArrayList<>();
        Peca completa = new Peca(10L, "Filtro", "Filtro de oleo", new BigDecimal("29.90"), 8, true, itens);
        assertThat(completa.getId()).isEqualTo(10L);
        assertThat(completa.getNome()).isEqualTo("Filtro");
        assertThat(completa.getDescricao()).isEqualTo("Filtro de oleo");
        assertThat(completa.getValorUnitario()).isEqualByComparingTo("29.90");
        assertThat(completa.getQuantidadeEstoque()).isEqualTo(8);
        assertThat(completa.getAtivo()).isTrue();
        assertThat(completa.getItensOrdem()).isSameAs(itens);
    }

    @Test
    void construtorVazioEGettersSettersDevemFuncionar() {
        Peca peca = new Peca();
        assertThat(peca.getQuantidadeEstoque()).isEqualTo(0);
        assertThat(peca.getAtivo()).isTrue();
        assertThat(peca.getItensOrdem()).isNotNull();

        List<ItemPecaOrdem> itens = new ArrayList<>();
        peca.setId(2L);
        peca.setNome("Pastilha");
        peca.setDescricao("Pastilha dianteira");
        peca.setValorUnitario(new BigDecimal("99.90"));
        peca.setQuantidadeEstoque(3);
        peca.setAtivo(false);
        peca.setItensOrdem(itens);

        assertThat(peca.getId()).isEqualTo(2L);
        assertThat(peca.getNome()).isEqualTo("Pastilha");
        assertThat(peca.getDescricao()).isEqualTo("Pastilha dianteira");
        assertThat(peca.getValorUnitario()).isEqualByComparingTo("99.90");
        assertThat(peca.getQuantidadeEstoque()).isEqualTo(3);
        assertThat(peca.getAtivo()).isFalse();
        assertThat(peca.getItensOrdem()).isSameAs(itens);
    }
}
