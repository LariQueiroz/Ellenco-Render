package com.ellenco.ellenco.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ellenco.ellenco.model.Foto;

public interface FotoRepository extends JpaRepository<Foto, Long> {

    /** Foto mais recente da obra (usada como capa do card). */
    Optional<Foto> findFirstByRegistroObraIdOrderByIdDesc(Long obraId);
}
