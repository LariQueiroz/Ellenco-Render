package com.ellenco.ellenco.model;

import jakarta.persistence.*;

@Entity
@Table(name = "foto_conteudo")
public class FotoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] dados;

    public FotoConteudo() {
    }

    public FotoConteudo(byte[] dados) {
        this.dados = dados;
    }

    public Long getId() { return id; }

    public byte[] getDados() { return dados; }
    public void setDados(byte[] dados) { this.dados = dados; }
}
