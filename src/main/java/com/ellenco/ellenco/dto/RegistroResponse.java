package com.ellenco.ellenco.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RegistroResponse(
        Long id,
        Long obraId,
        String obraNome,
        LocalDate data,
        LocalTime horario,
        String turno,
        String clima,
        String descricao,
        Double latitude,
        Double longitude,
        Integer progresso,
        String autor,
        List<FotoResponse> fotos) {
}
