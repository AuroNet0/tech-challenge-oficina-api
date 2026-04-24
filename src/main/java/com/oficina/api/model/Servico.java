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
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "tempo_estimado_minutos", nullable = false)
    private Integer tempoEstimadoMinutos;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "servico")
    private List<ItemServicoOrdem> itensOrdem = new ArrayList<>();

    public Servico() {
    }

    public Servico(Long id, String descricao, BigDecimal valor, Integer tempoEstimadoMinutos, Boolean ativo,
                   List<ItemServicoOrdem> itensOrdem) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.ativo = ativo;
        this.itensOrdem = itensOrdem;
    }

    @PrePersist
    public void prePersist() {
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    public void setTempoEstimadoMinutos(Integer tempoEstimadoMinutos) {
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<ItemServicoOrdem> getItensOrdem() {
        return itensOrdem;
    }

    public void setItensOrdem(List<ItemServicoOrdem> itensOrdem) {
        this.itensOrdem = itensOrdem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico servico)) return false;
        return id != null && Objects.equals(id, servico.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
