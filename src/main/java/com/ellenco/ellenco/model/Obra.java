package com.ellenco.ellenco.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ellenco.ellenco.util.Datas;

import jakarta.persistence.*;

@Entity
@Table(name = "obra")
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String localizacao;

    private String responsavel;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "previsao_termino")
    private LocalDate previsaoTermino;

    @Column(name = "data_finalizacao")
    private LocalDate dataFinalizacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusObra status = StatusObra.EM_ANDAMENTO;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    // 1 obra : N registros
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registro> registros = new ArrayList<>();

    public Obra() {
    }

    @PrePersist
    void antesDeSalvar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now(Datas.ZONA);
        }
        if (status == null) {
            status = StatusObra.EM_ANDAMENTO;
        }
    }

    public boolean isFinalizada() {
        return getStatus() == StatusObra.FINALIZADA;
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getPrevisaoTermino() { return previsaoTermino; }
    public void setPrevisaoTermino(LocalDate previsaoTermino) { this.previsaoTermino = previsaoTermino; }

    public LocalDate getDataFinalizacao() { return dataFinalizacao; }
    public void setDataFinalizacao(LocalDate dataFinalizacao) { this.dataFinalizacao = dataFinalizacao; }

    /** Obras antigas (sem status gravado) são tratadas como em andamento. */
    public StatusObra getStatus() { return status == null ? StatusObra.EM_ANDAMENTO : status; }
    public void setStatus(StatusObra status) { this.status = status; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public List<Registro> getRegistros() { return registros; }
    public void setRegistros(List<Registro> registros) { this.registros = registros; }
}
