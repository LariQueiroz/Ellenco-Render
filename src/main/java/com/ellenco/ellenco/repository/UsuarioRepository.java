package com.ellenco.ellenco.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ellenco.ellenco.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTokenRecuperacao(String token);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
