package com.ellenco.ellenco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ellenco.ellenco.model.Obra;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    List<Obra> findAllByOrderByIdDesc();
}
