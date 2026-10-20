package com.ellenco.ellenco.dto;

import java.time.LocalDate;

import com.ellenco.ellenco.model.StatusObra;

public record ObraResponse(
        Long id,
        String nome,
        String localizacao,
        String responsavel,
        String descricao,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataFinalizacao,
        StatusObra status,
        int progresso,
        long totalRegistros,
        LocalDate ultimoRegistro,
        String capaUrl,
        boolean atrasada) {
}
