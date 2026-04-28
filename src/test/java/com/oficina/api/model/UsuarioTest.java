package com.oficina.api.model;

import com.oficina.api.model.enums.PerfilUsuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void prePersistDeveAplicarDefaultQuandoAtivoForNulo() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(null);

        usuario.prePersist();

        assertThat(usuario.getAtivo()).isTrue();
    }

    @Test
    void prePersistNaoDeveSobrescreverAtivoQuandoJaPreenchido() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);

        usuario.prePersist();

        assertThat(usuario.getAtivo()).isFalse();
    }

    @Test
    void equalsHashCodeEConstrutoresDevemFuncionarCorretamente() {
        Usuario u1 = new Usuario();
        Usuario u2 = new Usuario();
        u1.setId(1L);
        u2.setId(1L);
        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        assertThat(u1.equals(null)).isFalse();
        assertThat(u1.equals("x")).isFalse();

        Usuario completo = new Usuario(10L, "Gerente", "gerente@email.com", "senha123", PerfilUsuario.GERENTE, true);
        assertThat(completo.getId()).isEqualTo(10L);
        assertThat(completo.getNome()).isEqualTo("Gerente");
        assertThat(completo.getEmail()).isEqualTo("gerente@email.com");
        assertThat(completo.getSenha()).isEqualTo("senha123");
        assertThat(completo.getPerfil()).isEqualTo(PerfilUsuario.GERENTE);
        assertThat(completo.getAtivo()).isTrue();
    }

    @Test
    void construtorVazioEGettersSettersDevemFuncionar() {
        Usuario usuario = new Usuario();
        assertThat(usuario.getAtivo()).isTrue();

        usuario.setId(2L);
        usuario.setNome("Atendente");
        usuario.setEmail("atendente@email.com");
        usuario.setSenha("segredo");
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        usuario.setAtivo(false);

        assertThat(usuario.getId()).isEqualTo(2L);
        assertThat(usuario.getNome()).isEqualTo("Atendente");
        assertThat(usuario.getEmail()).isEqualTo("atendente@email.com");
        assertThat(usuario.getSenha()).isEqualTo("segredo");
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.ATENDENTE);
        assertThat(usuario.getAtivo()).isFalse();
    }
}
