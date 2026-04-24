package com.oficina.api.model;

import com.oficina.api.model.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, unique = true)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(nullable = false)
    private String telefone;

    @Column(unique = true)
    private String email;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cliente")
    private List<Veiculo> veiculos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<OrdemServico> ordensServico = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(Long id, String nome, String cpfCnpj, TipoPessoa tipoPessoa, String telefone, String email,
                   LocalDateTime dataCadastro, List<Veiculo> veiculos, List<OrdemServico> ordensServico) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.tipoPessoa = tipoPessoa;
        this.telefone = telefone;
        this.email = email;
        this.dataCadastro = dataCadastro;
        this.veiculos = veiculos;
        this.ordensServico = ordensServico;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCadastro == null) {
            this.dataCadastro = LocalDateTime.now();
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

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public List<OrdemServico> getOrdensServico() {
        return ordensServico;
    }

    public void setOrdensServico(List<OrdemServico> ordensServico) {
        this.ordensServico = ordensServico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente cliente)) return false;
        return id != null && Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
