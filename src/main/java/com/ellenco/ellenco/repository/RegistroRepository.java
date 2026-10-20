package com.ellenco.ellenco.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ellenco.ellenco.model.Registro;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

    List<Registro> findByObraIdOrderByDataAsc(Long obraId);

    List<Registro> findByObraIdOrderByDataDescIdDesc(Long obraId);

    List<Registro> findByObraIdAndDataBetweenOrderByDataAscHorarioAscIdAsc(
            Long obraId, LocalDate inicio, LocalDate fim);

    long countByObraId(Long obraId);

    long countByDataBetween(LocalDate inicio, LocalDate fim);

    Optional<Registro> findFirstByObraIdOrderByDataDescIdDesc(Long obraId);

    Optional<Registro> findFirstByObraIdOrderByDataAscIdAsc(Long obraId);

    Optional<Registro> findFirstByObraIdAndProgressoIsNotNullOrderByDataDescIdDesc(Long obraId);

    Optional<Registro> findFirstByObraIdAndProgressoIsNotNullAndDataLessThanEqualOrderByDataDescIdDesc(
            Long obraId, LocalDate ate);

    @Query("select r.clima, count(r) from Registro r group by r.clima")
    List<Object[]> contarPorClima();

    @Query("select r.turno, count(r) from Registro r group by r.turno")
    List<Object[]> contarPorTurno();

    @Query("select r.obra.id, max(r.data) from Registro r group by r.obra.id")
    List<Object[]> ultimaDataPorObra();

    @Query("select r.data from Registro r where r.data >= :inicio")
    List<LocalDate> datasDesde(@Param("inicio") LocalDate inicio);

    @Query("select r from Registro r join fetch r.obra order by r.data desc, r.id desc")
    List<Registro> ultimos(Pageable pageable);

    @Query("select r from Registro r join fetch r.obra "
            + "where r.latitude is not null and r.longitude is not null "
            + "order by r.data desc, r.id desc")
    List<Registro> comLocalizacao(Pageable pageable);
}
