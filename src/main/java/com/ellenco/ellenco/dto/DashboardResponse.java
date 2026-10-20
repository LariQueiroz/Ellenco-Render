package com.ellenco.ellenco.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        Totais totais,
        List<SemanaContagem> registrosPorSemana,
        List<Contagem> clima,
        List<Contagem> turno,
        List<ObraProgresso> obrasEmAndamento,
        List<RegistroResponse> ultimosRegistros,
        List<PontoMapa> pontos) {

    public record Totais(
            long obras,
            long emAndamento,
            long finalizadas,
            long registros,
            long registrosSemana,
            long fotos,
            long semRegistroRecente) {
    }

    public record SemanaContagem(LocalDate inicio, long total) {
    }

    public record Contagem(String nome, long total) {
    }

    public record ObraProgresso(
            Long id,
            String nome,
            String localizacao,
            int progresso,
            LocalDate ultimoRegistro,
            Long diasSemRegistro,
            boolean semRegistroRecente,
            boolean atrasada) {
    }

    public record PontoMapa(
            Long obraId,
            String obraNome,
            Double latitude,
            Double longitude,
            LocalDate data) {
    }
}
