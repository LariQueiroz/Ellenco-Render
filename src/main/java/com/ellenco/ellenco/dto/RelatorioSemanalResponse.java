package com.ellenco.ellenco.dto;

import java.time.LocalDate;
import java.util.List;

public record RelatorioSemanalResponse(
        ObraResponse obra,
        LocalDate inicio,
        LocalDate fim,
        LocalDate semanaAnterior,
        LocalDate proximaSemana,
        LocalDate primeiroRegistro,
        Integer progressoAtual,
        Integer variacaoProgresso,
        long diasTrabalhados,
        long totalRegistros,
        long totalFotos,
        List<RegistroResponse> registros) {
}
