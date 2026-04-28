package com.oficina.api.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ItemPecaOrdemTest {

    @Test
    void equalsDeveRetornarTrueParaMesmaInstancia() {
        ItemPecaOrdem item = new ItemPecaOrdem();

        assertThat(item).isEqualTo(item);
    }

    @Test
    void equalsDeveRetornarFalseParaNull() {
        ItemPecaOrdem item = new ItemPecaOrdem();

        assertThat(item.equals(null)).isFalse();
    }

    @Test
    void equalsDeveRetornarFalseParaOutroTipo() {
        ItemPecaOrdem item = new ItemPecaOrdem();

        assertThat(item.equals("nao-e-item")).isFalse();
    }

    @Test
    void equalsDeveRetornarTrueParaIdsIguaisENaoNulos() {
        ItemPecaOrdem item1 = new ItemPecaOrdem();
        item1.setId(1L);
        ItemPecaOrdem item2 = new ItemPecaOrdem();
        item2.setId(1L);

        assertThat(item1).isEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseParaIdsDiferentes() {
        ItemPecaOrdem item1 = new ItemPecaOrdem();
        item1.setId(1L);
        ItemPecaOrdem item2 = new ItemPecaOrdem();
        item2.setId(2L);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoIdAtualForNullEOutraInstancia() {
        ItemPecaOrdem item1 = new ItemPecaOrdem();
        item1.setId(null);
        ItemPecaOrdem item2 = new ItemPecaOrdem();
        item2.setId(1L);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoAmbosIdsForemNullEmInstanciasDiferentes() {
        ItemPecaOrdem item1 = new ItemPecaOrdem();
        ItemPecaOrdem item2 = new ItemPecaOrdem();

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void hashCodeDeveSerConsistenteNaMesmaInstancia() {
        ItemPecaOrdem item = new ItemPecaOrdem();

        int hash1 = item.hashCode();
        int hash2 = item.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashCodeDeveSerIgualParaInstanciasDaMesmaClasse() {
        ItemPecaOrdem item1 = new ItemPecaOrdem();
        ItemPecaOrdem item2 = new ItemPecaOrdem();

        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    void construtorCompletoDeveAtribuirTodosOsCampos() {
        OrdemServico ordemServico = new OrdemServico();
        Peca peca = new Peca();
        BigDecimal valorUnitario = new BigDecimal("59.90");

        ItemPecaOrdem item = new ItemPecaOrdem(
                10L,
                ordemServico,
                peca,
                3,
                valorUnitario
        );

        assertThat(item.getId()).isEqualTo(10L);
        assertThat(item.getOrdemServico()).isSameAs(ordemServico);
        assertThat(item.getPeca()).isSameAs(peca);
        assertThat(item.getQuantidade()).isEqualTo(3);
        assertThat(item.getValorUnitario()).isEqualByComparingTo("59.90");
    }

    @Test
    void gettersESettersDevemFuncionarCorretamente() {
        ItemPecaOrdem item = new ItemPecaOrdem();
        OrdemServico ordemServico = new OrdemServico();
        Peca peca = new Peca();
        BigDecimal valorUnitario = new BigDecimal("19.90");

        item.setId(20L);
        item.setOrdemServico(ordemServico);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorUnitario(valorUnitario);

        assertThat(item.getId()).isEqualTo(20L);
        assertThat(item.getOrdemServico()).isSameAs(ordemServico);
        assertThat(item.getPeca()).isSameAs(peca);
        assertThat(item.getQuantidade()).isEqualTo(2);
        assertThat(item.getValorUnitario()).isEqualByComparingTo("19.90");
    }
}
