package com.oficina.api.model;

import com.oficina.api.model.enums.TipoPessoa;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteTest {

    @Test
    void prePersistDevePreencherDataCadastroQuandoNula() {
        Cliente cliente = new Cliente();
        cliente.setDataCadastro(null);

        cliente.prePersist();

        assertThat(cliente.getDataCadastro()).isNotNull();
    }

    @Test
    void prePersistNaoDeveSobrescreverDataCadastroQuandoJaPreenchida() {
        LocalDateTime dataOriginal = LocalDateTime.of(2026, 1, 1, 10, 0);
        Cliente cliente = new Cliente();
        cliente.setDataCadastro(dataOriginal);

        cliente.prePersist();

        assertThat(cliente.getDataCadastro()).isEqualTo(dataOriginal);
    }

    @Test
    void equalsDeveRetornarTrueParaMesmaInstancia() {
        Cliente cliente = new Cliente();

        assertThat(cliente).isEqualTo(cliente);
    }

    @Test
    void equalsDeveRetornarFalseParaNull() {
        Cliente cliente = new Cliente();

        assertThat(cliente.equals(null)).isFalse();
    }

    @Test
    void equalsDeveRetornarFalseParaOutroTipo() {
        Cliente cliente = new Cliente();

        assertThat(cliente.equals("nao-e-cliente")).isFalse();
    }

    @Test
    void equalsDeveRetornarTrueParaIdsIguaisENaoNulos() {
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        Cliente cliente2 = new Cliente();
        cliente2.setId(1L);

        assertThat(cliente1).isEqualTo(cliente2);
    }

    @Test
    void equalsDeveRetornarFalseParaIdsDiferentes() {
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);

        assertThat(cliente1).isNotEqualTo(cliente2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoIdAtualForNullEOutraInstancia() {
        Cliente cliente1 = new Cliente();
        cliente1.setId(null);
        Cliente cliente2 = new Cliente();
        cliente2.setId(1L);

        assertThat(cliente1).isNotEqualTo(cliente2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoAmbosIdsForemNullEmInstanciasDiferentes() {
        Cliente cliente1 = new Cliente();
        Cliente cliente2 = new Cliente();

        assertThat(cliente1).isNotEqualTo(cliente2);
    }

    @Test
    void hashCodeDeveSerConsistenteNaMesmaInstancia() {
        Cliente cliente = new Cliente();

        int hash1 = cliente.hashCode();
        int hash2 = cliente.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashCodeDeveSerIgualParaInstanciasDaMesmaClasse() {
        Cliente cliente1 = new Cliente();
        Cliente cliente2 = new Cliente();

        assertThat(cliente1.hashCode()).isEqualTo(cliente2.hashCode());
    }

    @Test
    void construtorVazioDeveIniciarListasNaoNulas() {
        Cliente cliente = new Cliente();

        assertThat(cliente.getVeiculos()).isNotNull();
        assertThat(cliente.getOrdensServico()).isNotNull();
    }

    @Test
    void construtorCompletoDeveAtribuirTodosOsCampos() {
        LocalDateTime dataCadastro = LocalDateTime.of(2026, 3, 15, 8, 30);
        List<Veiculo> veiculos = new ArrayList<>();
        List<OrdemServico> ordensServico = new ArrayList<>();

        Cliente cliente = new Cliente(
                10L,
                "Maria",
                "12345678901",
                TipoPessoa.PF,
                "11999998888",
                "maria@email.com",
                dataCadastro,
                veiculos,
                ordensServico
        );

        assertThat(cliente.getId()).isEqualTo(10L);
        assertThat(cliente.getNome()).isEqualTo("Maria");
        assertThat(cliente.getCpfCnpj()).isEqualTo("12345678901");
        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.PF);
        assertThat(cliente.getTelefone()).isEqualTo("11999998888");
        assertThat(cliente.getEmail()).isEqualTo("maria@email.com");
        assertThat(cliente.getDataCadastro()).isEqualTo(dataCadastro);
        assertThat(cliente.getVeiculos()).isSameAs(veiculos);
        assertThat(cliente.getOrdensServico()).isSameAs(ordensServico);
    }
}
