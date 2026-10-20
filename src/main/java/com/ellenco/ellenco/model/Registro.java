package com.ellenco.ellenco.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ellenco.ellenco.util.Datas;

import jakarta.persistence.*;

@Entity
@Table(name = "registro")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @Column(nullable = false)
    private LocalDate data;

    private LocalTime horario;

    private String turno;

    private String clima;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private Double latitude;

    private Double longitude;

    /** Progresso acumulado da obra (0 a 100) informado neste registro. */
    private Integer progresso;

    /** Nome de quem lançou o registro. */
    private String autor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    // 1 registro : N fotos
    @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Foto> fotos = new ArrayList<>();

    public Registro() {
    }

    @PrePersist
    void antesDeSalvar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now(Datas.ZONA);
        }
    }

    public Long getId() { return id; }

    public Obra getObra() { return obra; }
    public void setObra(Obra obra) { this.obra = obra; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public String getClima() { return clima; }
    public void setClima(String clima) { this.clima = clima; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getProgresso() { return progresso; }
    public void setProgresso(Integer progresso) { this.progresso = progresso; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public List<Foto> getFotos() { return fotos; }
    public void setFotos(List<Foto> fotos) { this.fotos = fotos; }
}
