package com.ellenco.ellenco.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ellenco.ellenco.dto.DashboardResponse;
import com.ellenco.ellenco.dto.DashboardResponse.Contagem;
import com.ellenco.ellenco.dto.DashboardResponse.ObraProgresso;
import com.ellenco.ellenco.dto.DashboardResponse.PontoMapa;
import com.ellenco.ellenco.dto.DashboardResponse.SemanaContagem;
import com.ellenco.ellenco.dto.DashboardResponse.Totais;
import com.ellenco.ellenco.model.Obra;
import com.ellenco.ellenco.model.Registro;
import com.ellenco.ellenco.repository.FotoRepository;
import com.ellenco.ellenco.repository.ObraRepository;
import com.ellenco.ellenco.repository.RegistroRepository;
import com.ellenco.ellenco.util.Datas;

@Service
public class DashboardService {

    private static final int SEMANAS = 8;

    private final ObraRepository obraRepository;
    private final RegistroRepository registroRepository;
    private final FotoRepository fotoRepository;

    public DashboardService(ObraRepository obraRepository,
            RegistroRepository registroRepository,
            FotoRepository fotoRepository) {
        this.obraRepository = obraRepository;
        this.registroRepository = registroRepository;
        this.fotoRepository = fotoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse montar() {
        LocalDate hoje = Datas.hoje();
        LocalDate inicioSemana = Datas.inicioDaSemana(hoje);

        // ---- última data de registro por obra
        Map<Long, LocalDate> ultimaData = new HashMap<>();
        for (Object[] linha : registroRepository.ultimaDataPorObra()) {
            ultimaData.put((Long) linha[0], (LocalDate) linha[1]);
        }

        // ---- obras
        List<Obra> obras = obraRepository.findAllByOrderByIdDesc();
        long emAndamento = 0;
        long finalizadas = 0;
        long semRegistro = 0;
        List<ObraProgresso> andamento = new ArrayList<>();

        for (Obra o : obras) {
            if (o.isFinalizada()) {
                finalizadas++;
                continue;
            }
            emAndamento++;

            LocalDate ultimo = ultimaData.get(o.getId());
            boolean semRecente = ultimo == null || ultimo.isBefore(hoje.minusDays(6));
            if (semRecente) {
                semRegistro++;
            }
            int progresso = registroRepository
                    .findFirstByObraIdAndProgressoIsNotNullOrderByDataDescIdDesc(o.getId())
                    .map(Registro::getProgresso).orElse(0);
            boolean atrasada = o.getPrevisaoTermino() != null && o.getPrevisaoTermino().isBefore(hoje);

            andamento.add(new ObraProgresso(
                    o.getId(), o.getNome(), o.getLocalizacao(), progresso, ultimo,
                    ultimo == null ? null : ChronoUnit.DAYS.between(ultimo, hoje),
                    semRecente, atrasada));
        }

        // ---- registros por semana (últimas 8 semanas)
        LocalDate primeiraSemana = inicioSemana.minusWeeks(SEMANAS - 1L);
        Map<LocalDate, Long> porSemana = new LinkedHashMap<>();
        for (int i = 0; i < SEMANAS; i++) {
            porSemana.put(primeiraSemana.plusWeeks(i), 0L);
        }
        for (LocalDate d : registroRepository.datasDesde(primeiraSemana)) {
            porSemana.computeIfPresent(Datas.inicioDaSemana(d), (k, v) -> v + 1);
        }
        List<SemanaContagem> semanas = porSemana.entrySet().stream()
                .map(e -> new SemanaContagem(e.getKey(), e.getValue()))
                .toList();

        long registrosSemana = registroRepository.countByDataBetween(inicioSemana, inicioSemana.plusDays(6));

        Totais totais = new Totais(
                obras.size(), emAndamento, finalizadas,
                registroRepository.count(), registrosSemana,
                fotoRepository.count(), semRegistro);

        // ---- últimos registros
        List<com.ellenco.ellenco.dto.RegistroResponse> ultimos = registroRepository
                .ultimos(PageRequest.of(0, 6)).stream().map(Mapeador::registro).toList();

        // ---- mapa: último ponto com coordenadas de cada obra
        Map<Long, PontoMapa> pontos = new LinkedHashMap<>();
        for (Registro r : registroRepository.comLocalizacao(PageRequest.of(0, 300))) {
            Long obraId = r.getObra().getId();
            pontos.putIfAbsent(obraId, new PontoMapa(
                    obraId, r.getObra().getNome(), r.getLatitude(), r.getLongitude(), r.getData()));
        }

        return new DashboardResponse(
                totais, semanas,
                contagens(registroRepository.contarPorClima()),
                contagens(registroRepository.contarPorTurno()),
                andamento, ultimos, new ArrayList<>(pontos.values()));
    }

    private static List<Contagem> contagens(List<Object[]> linhas) {
        return linhas.stream()
                .map(l -> new Contagem(
                        l[0] == null || l[0].toString().isBlank() ? "Não informado" : l[0].toString(),
                        ((Number) l[1]).longValue()))
                .sorted((a, b) -> Long.compare(b.total(), a.total()))
                .toList();
    }
}
