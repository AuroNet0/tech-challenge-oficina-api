package com.oficina.api.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ItemServicoOrdemTest {

    @Test
    void equalsDeveRetornarTrueParaMesmaInstancia() {
        ItemServicoOrdem item = new ItemServicoOrdem();

        assertThat(item).isEqualTo(item);
    }

    @Test
    void equalsDeveRetornarFalseParaNull() {
        ItemServicoOrdem item = new ItemServicoOrdem();

        assertThat(item.equals(null)).isFalse();
    }

    @Test
    void equalsDeveRetornarFalseParaOutroTipo() {
        ItemServicoOrdem item = new ItemServicoOrdem();

        assertThat(item.equals("nao-e-item")).isFalse();
    }

    @Test
    void equalsDeveRetornarTrueParaIdsIguaisENaoNulos() {
        ItemServicoOrdem item1 = new ItemServicoOrdem();
        item1.setId(1L);
        ItemServicoOrdem item2 = new ItemServicoOrdem();
        item2.setId(1L);

        assertThat(item1).isEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseParaIdsDiferentes() {
        ItemServicoOrdem item1 = new ItemServicoOrdem();
        item1.setId(1L);
        ItemServicoOrdem item2 = new ItemServicoOrdem();
        item2.setId(2L);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoIdAtualForNullEOutraInstancia() {
        ItemServicoOrdem item1 = new ItemServicoOrdem();
        item1.setId(null);
        ItemServicoOrdem item2 = new ItemServicoOrdem();
        item2.setId(1L);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoAmbosIdsForemNullEmInstanciasDiferentes() {
        ItemServicoOrdem item1 = new ItemServicoOrdem();
        ItemServicoOrdem item2 = new ItemServicoOrdem();

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void hashCodeDeveSerConsistenteNaMesmaInstancia() {
        ItemServicoOrdem item = new ItemServicoOrdem();

        int hash1 = item.hashCode();
        int hash2 = item.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashCodeDeveSerIgualParaInstanciasDaMesmaClasse() {
        ItemServicoOrdem item1 = new ItemServicoOrdem();
        ItemServicoOrdem item2 = new ItemServicoOrdem();

        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    void construtorCompletoDeveAtribuirTodosOsCampos() {
        OrdemServico ordemServico = new OrdemServico();
        Servico servico = new Servico();
        BigDecimal valor = new BigDecimal("199.90");

        ItemServicoOrdem item = new ItemServicoOrdem(
                10L,
                ordemServico,
                servico,
                valor,
                "Troca de filtro"
        );

        assertThat(item.getId()).isEqualTo(10L);
        assertThat(item.getOrdemServico()).isSameAs(ordemServico);
        assertThat(item.getServico()).isSameAs(servico);
        assertThat(item.getValor()).isEqualByComparingTo("199.90");
        assertThat(item.getObservacao()).isEqualTo("Troca de filtro");
    }

    @Test
    void gettersESettersDevemFuncionarCorretamente() {
        ItemServicoOrdem item = new ItemServicoOrdem();
        OrdemServico ordemServico = new OrdemServico();
        Servico servico = new Servico();
        BigDecimal valor = new BigDecimal("89.50");

        item.setId(20L);
        item.setOrdemServico(ordemServico);
        item.setServico(servico);
        item.setValor(valor);
        item.setObservacao("Alinhamento");

        assertThat(item.getId()).isEqualTo(20L);
        assertThat(item.getOrdemServico()).isSameAs(ordemServico);
        assertThat(item.getServico()).isSameAs(servico);
        assertThat(item.getValor()).isEqualByComparingTo("89.50");
        assertThat(item.getObservacao()).isEqualTo("Alinhamento");
    }
}
