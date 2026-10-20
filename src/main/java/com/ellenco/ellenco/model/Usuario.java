package com.ellenco.ellenco.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // DADOS DO USUÁRIO
    // ==========================================

    @Column(nullable = false)
    private String nome;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false, unique = true)
    private String login;


    @Column(nullable = false)
    private String senha;


    // ==========================================
    // CARGO
    // ==========================================

    @Column(length = 100)
    private String cargo;


    // ==========================================
    // RECUPERAÇÃO DE SENHA
    // ==========================================

    @Column(name = "token_recuperacao")
    private String tokenRecuperacao;


    @Column(name = "token_expiracao")
    private Long tokenExpiracao;


    // ==========================================
    // CONSTRUTOR
    // ==========================================

    public Usuario() {
    }


    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

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


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getLogin() {
        return login;
    }


    public void setLogin(String login) {
        this.login = login;
    }


    public String getSenha() {
        return senha;
    }


    public void setSenha(String senha) {
        this.senha = senha;
    }


    public String getCargo() {
        return cargo;
    }


    public void setCargo(String cargo) {
        this.cargo = cargo;
    }


    public String getTokenRecuperacao() {
        return tokenRecuperacao;
    }


    public void setTokenRecuperacao(String tokenRecuperacao) {
        this.tokenRecuperacao = tokenRecuperacao;
    }


    public Long getTokenExpiracao() {
        return tokenExpiracao;
    }


    public void setTokenExpiracao(Long tokenExpiracao) {
        this.tokenExpiracao = tokenExpiracao;
    }

}
