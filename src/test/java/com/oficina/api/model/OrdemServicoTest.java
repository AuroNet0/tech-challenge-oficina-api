package com.oficina.api.model;

import com.oficina.api.model.enums.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoTest {

    @Test
    void prePersistDeveAplicarDefaultsQuandoCamposForemNulos() {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatus(null);
        ordemServico.setDataAbertura(null);
        ordemServico.setValorTotal(null);

        ordemServico.prePersist();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(ordemServico.getDataAbertura()).isNotNull();
        assertThat(ordemServico.getValorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void prePersistNaoDeveSobrescreverCamposQuandoJaPreenchidos() {
        LocalDateTime dataAbertura = LocalDateTime.of(2026, 2, 10, 14, 0);
        BigDecimal valorTotal = new BigDecimal("350.75");
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDataAbertura(dataAbertura);
        ordemServico.setValorTotal(valorTotal);

        ordemServico.prePersist();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(ordemServico.getDataAbertura()).isEqualTo(dataAbertura);
        assertThat(ordemServico.getValorTotal()).isEqualByComparingTo("350.75");
    }

    @Test
    void equalsDeveRetornarTrueParaMesmaInstancia() {
        OrdemServico ordemServico = new OrdemServico();

        assertThat(ordemServico).isEqualTo(ordemServico);
    }

    @Test
    void equalsDeveRetornarFalseParaNull() {
        OrdemServico ordemServico = new OrdemServico();

        assertThat(ordemServico.equals(null)).isFalse();
    }

    @Test
    void equalsDeveRetornarFalseParaOutroTipo() {
        OrdemServico ordemServico = new OrdemServico();

        assertThat(ordemServico.equals("nao-e-ordem")).isFalse();
    }

    @Test
    void equalsDeveRetornarTrueParaIdsIguaisENaoNulos() {
        OrdemServico ordem1 = new OrdemServico();
        ordem1.setId(1L);
        OrdemServico ordem2 = new OrdemServico();
        ordem2.setId(1L);

        assertThat(ordem1).isEqualTo(ordem2);
    }

    @Test
    void equalsDeveRetornarFalseParaIdsDiferentes() {
        OrdemServico ordem1 = new OrdemServico();
        ordem1.setId(1L);
        OrdemServico ordem2 = new OrdemServico();
        ordem2.setId(2L);

        assertThat(ordem1).isNotEqualTo(ordem2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoIdAtualForNullEOutraInstancia() {
        OrdemServico ordem1 = new OrdemServico();
        ordem1.setId(null);
        OrdemServico ordem2 = new OrdemServico();
        ordem2.setId(1L);

        assertThat(ordem1).isNotEqualTo(ordem2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoAmbosIdsForemNullEmInstanciasDiferentes() {
        OrdemServico ordem1 = new OrdemServico();
        OrdemServico ordem2 = new OrdemServico();

        assertThat(ordem1).isNotEqualTo(ordem2);
    }

    @Test
    void hashCodeDeveSerConsistenteNaMesmaInstancia() {
        OrdemServico ordemServico = new OrdemServico();

        int hash1 = ordemServico.hashCode();
        int hash2 = ordemServico.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashCodeDeveSerIgualParaInstanciasDaMesmaClasse() {
        OrdemServico ordem1 = new OrdemServico();
        OrdemServico ordem2 = new OrdemServico();

        assertThat(ordem1.hashCode()).isEqualTo(ordem2.hashCode());
    }

    @Test
    void construtorVazioDeveIniciarCamposDefaultENaoNulosOndeAplicavel() {
        OrdemServico ordemServico = new OrdemServico();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(ordemServico.getValorTotal()).isEqualByComparingTo("0");
        assertThat(ordemServico.getItensServico()).isNotNull();
        assertThat(ordemServico.getItensPeca()).isNotNull();
    }

    @Test
    void construtorCompletoDeveAtribuirTodosOsCampos() {
        Cliente cliente = new Cliente();
        Veiculo veiculo = new Veiculo();
        LocalDateTime dataAbertura = LocalDateTime.of(2026, 3, 1, 9, 0);
        LocalDateTime dataFinalizacao = LocalDateTime.of(2026, 3, 2, 18, 30);
        BigDecimal valorTotal = new BigDecimal("980.40");
        List<ItemServicoOrdem> itensServico = new ArrayList<>();
        List<ItemPecaOrdem> itensPeca = new ArrayList<>();

        OrdemServico ordemServico = new OrdemServico(
                10L,
                cliente,
                veiculo,
                StatusOrdemServico.FINALIZADA,
                dataAbertura,
                dataFinalizacao,
                valorTotal,
                "Observacao teste",
                itensServico,
                itensPeca
        );

        assertThat(ordemServico.getId()).isEqualTo(10L);
        assertThat(ordemServico.getCliente()).isSameAs(cliente);
        assertThat(ordemServico.getVeiculo()).isSameAs(veiculo);
        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(ordemServico.getDataAbertura()).isEqualTo(dataAbertura);
        assertThat(ordemServico.getDataFinalizacao()).isEqualTo(dataFinalizacao);
        assertThat(ordemServico.getValorTotal()).isEqualByComparingTo("980.40");
        assertThat(ordemServico.getObservacoes()).isEqualTo("Observacao teste");
        assertThat(ordemServico.getItensServico()).isSameAs(itensServico);
        assertThat(ordemServico.getItensPeca()).isSameAs(itensPeca);
    }
}
