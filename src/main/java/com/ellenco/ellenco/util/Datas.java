package com.ellenco.ellenco.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Utilidades de data. O servidor (Render/Docker) roda em UTC, por isso
 * "hoje" é sempre calculado no fuso de São Paulo.
 */
public final class Datas {

    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Datas() {
    }

    public static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }

    /** Segunda-feira da semana da data informada. */
    public static LocalDate inicioDaSemana(LocalDate data) {
        return data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
