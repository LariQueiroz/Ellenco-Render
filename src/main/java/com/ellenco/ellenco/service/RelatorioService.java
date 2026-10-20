package com.ellenco.ellenco.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.dto.RelatorioSemanalResponse;
import com.ellenco.ellenco.model.Obra;
import com.ellenco.ellenco.model.Registro;
import com.ellenco.ellenco.repository.RegistroRepository;
import com.ellenco.ellenco.util.Datas;

@Service
public class RelatorioService {

    private final ObraService obraService;
    private final RegistroRepository registroRepository;

    public RelatorioService(ObraService obraService, RegistroRepository registroRepository) {
        this.obraService = obraService;
        this.registroRepository = registroRepository;
    }

    /**
     * Relatório da semana (segunda a domingo) que contém a data de referência.
     * Sem referência, usa a semana atual.
     */
    @Transactional(readOnly = true)
    public RelatorioSemanalResponse semanal(Long obraId, LocalDate referencia) {
        Obra obra = obraService.obter(obraId);

        LocalDate hoje = Datas.hoje();
        LocalDate inicio = Datas.inicioDaSemana(referencia != null ? referencia : hoje);
        LocalDate fim = inicio.plusDays(6);

        List<Registro> registros = registroRepository
                .findByObraIdAndDataBetweenOrderByDataAscHorarioAscIdAsc(obraId, inicio, fim);
        List<RegistroResponse> lista = registros.stream().map(Mapeador::registro).toList();

        long dias = registros.stream().map(Registro::getData).distinct().count();
        long fotos = lista.stream().mapToLong(r -> r.fotos().size()).sum();

        Integer atual = progressoAte(obra, fim);
        Integer anterior = progressoAte(obra, inicio.minusDays(1));
        Integer variacao = (atual != null && anterior != null) ? atual - anterior
                : (atual != null ? atual : null);

        LocalDate proxima = inicio.plusWeeks(1);
        if (proxima.isAfter(hoje)) {
            proxima = null;
        }

        LocalDate primeiro = registroRepository.findFirstByObraIdOrderByDataAscIdAsc(obraId)
                .map(Registro::getData).orElse(null);

        return new RelatorioSemanalResponse(
                obraService.paraResposta(obra),
                inicio, fim,
                inicio.minusWeeks(1), proxima,
                primeiro,
                atual, variacao,
                dias, lista.size(), fotos, lista);
    }

    /** Último progresso informado até a data (100 se a obra já estava finalizada). */
    private Integer progressoAte(Obra obra, LocalDate ate) {
        if (obra.isFinalizada() && obra.getDataFinalizacao() != null
                && !obra.getDataFinalizacao().isAfter(ate)) {
            return 100;
        }
        return registroRepository
                .findFirstByObraIdAndProgressoIsNotNullAndDataLessThanEqualOrderByDataDescIdDesc(
                        obra.getId(), ate)
                .map(Registro::getProgresso)
                .orElse(null);
    }
}
