package com.oficina.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoTest {

    @Test
    void equalsHashCodeEConstrutoresDevemFuncionarCorretamente() {
        Veiculo v1 = new Veiculo();
        Veiculo v2 = new Veiculo();
        v1.setId(1L);
        v2.setId(1L);
        assertThat(v1).isEqualTo(v2);
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        assertThat(v1.equals(null)).isFalse();
        assertThat(v1.equals("x")).isFalse();

        Cliente cliente = new Cliente();
        List<OrdemServico> ordens = new ArrayList<>();
        Veiculo completo = new Veiculo(10L, "ABC1D23", "Ford", "Ka", 2020, cliente, ordens);
        assertThat(completo.getId()).isEqualTo(10L);
        assertThat(completo.getPlaca()).isEqualTo("ABC1D23");
        assertThat(completo.getMarca()).isEqualTo("Ford");
        assertThat(completo.getModelo()).isEqualTo("Ka");
        assertThat(completo.getAno()).isEqualTo(2020);
        assertThat(completo.getCliente()).isSameAs(cliente);
        assertThat(completo.getOrdensServico()).isSameAs(ordens);
    }

    @Test
    void construtorVazioEGettersSettersDevemFuncionar() {
        Veiculo veiculo = new Veiculo();
        assertThat(veiculo.getOrdensServico()).isNotNull();

        Cliente cliente = new Cliente();
        List<OrdemServico> ordens = new ArrayList<>();
        veiculo.setId(2L);
        veiculo.setPlaca("XYZ9K87");
        veiculo.setMarca("Volkswagen");
        veiculo.setModelo("Gol");
        veiculo.setAno(2018);
        veiculo.setCliente(cliente);
        veiculo.setOrdensServico(ordens);

        assertThat(veiculo.getId()).isEqualTo(2L);
        assertThat(veiculo.getPlaca()).isEqualTo("XYZ9K87");
        assertThat(veiculo.getMarca()).isEqualTo("Volkswagen");
        assertThat(veiculo.getModelo()).isEqualTo("Gol");
        assertThat(veiculo.getAno()).isEqualTo(2018);
        assertThat(veiculo.getCliente()).isSameAs(cliente);
        assertThat(veiculo.getOrdensServico()).isSameAs(ordens);
    }
}
