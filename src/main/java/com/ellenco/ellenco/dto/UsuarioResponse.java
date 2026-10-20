package com.ellenco.ellenco.dto;

import com.ellenco.ellenco.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, String login, String cargo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getCargo());
    }
}
