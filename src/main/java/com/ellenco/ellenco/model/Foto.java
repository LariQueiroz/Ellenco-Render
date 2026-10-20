package com.ellenco.ellenco.model;

import jakarta.persistence.*;

/**
 * Metadados da foto. Os bytes ficam em {@link FotoConteudo} (carregado sob
 * demanda), assim listar registros nunca puxa imagens para a memória.
 *
 * As imagens são guardadas no MySQL porque o disco do Render é efêmero:
 * arquivos gravados em pasta local somem a cada deploy/reinício.
 */
@Entity
@Table(name = "foto")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_id", nullable = false)
    private Registro registro;

    /**
     * Mantido por compatibilidade com o banco anterior (coluna NOT NULL).
     * Fotos novas gravam "/api/fotos/{id}"; fotos antigas, "/uploads/arquivo".
     */
    @Column(nullable = false, length = 500)
    private String caminho;

    @Column(name = "nome_arquivo")
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    private Long tamanho;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "conteudo_id")
    private FotoConteudo conteudo;

    public Foto() {
    }

    public Long getId() { return id; }

    public Registro getRegistro() { return registro; }
    public void setRegistro(Registro registro) { this.registro = registro; }

    public String getCaminho() { return caminho; }
    public void setCaminho(String caminho) { this.caminho = caminho; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }

    public Long getTamanho() { return tamanho; }
    public void setTamanho(Long tamanho) { this.tamanho = tamanho; }

    public FotoConteudo getConteudo() { return conteudo; }
    public void setConteudo(FotoConteudo conteudo) { this.conteudo = conteudo; }
}
