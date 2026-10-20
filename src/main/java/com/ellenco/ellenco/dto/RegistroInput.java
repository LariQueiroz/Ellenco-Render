package com.ellenco.ellenco.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados de um novo registro (vindos do formulário multipart). */
public record RegistroInput(
        LocalDate data,
        LocalTime horario,
        String turno,
        String clima,
        String descricao,
        Double latitude,
        Double longitude,
        Integer progresso) {
}
