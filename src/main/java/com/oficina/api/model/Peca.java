package com.oficina.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pecas")
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "peca")
    private List<ItemPecaOrdem> itensOrdem = new ArrayList<>();

    public Peca() {
    }

    public Peca(Long id, String nome, String descricao, BigDecimal valorUnitario, Integer quantidadeEstoque,
                Boolean ativo, List<ItemPecaOrdem> itensOrdem) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.valorUnitario = valorUnitario;
        this.quantidadeEstoque = quantidadeEstoque;
        this.ativo = ativo;
        this.itensOrdem = itensOrdem;
    }

    @PrePersist
    public void prePersist() {
        if (this.quantidadeEstoque == null) {
            this.quantidadeEstoque = 0;
        }
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<ItemPecaOrdem> getItensOrdem() {
        return itensOrdem;
    }

    public void setItensOrdem(List<ItemPecaOrdem> itensOrdem) {
        this.itensOrdem = itensOrdem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peca peca)) return false;
        return id != null && Objects.equals(id, peca.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
