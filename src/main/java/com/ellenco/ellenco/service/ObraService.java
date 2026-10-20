package com.ellenco.ellenco.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ellenco.ellenco.dto.ObraRequest;
import com.ellenco.ellenco.dto.ObraResponse;
import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.exception.NaoEncontradoException;
import com.ellenco.ellenco.exception.NegocioException;
import com.ellenco.ellenco.model.Obra;
import com.ellenco.ellenco.model.Registro;
import com.ellenco.ellenco.model.StatusObra;
import com.ellenco.ellenco.repository.FotoRepository;
import com.ellenco.ellenco.repository.ObraRepository;
import com.ellenco.ellenco.repository.RegistroRepository;
import com.ellenco.ellenco.util.Datas;

@Service
public class ObraService {

    private final ObraRepository obraRepository;
    private final RegistroRepository registroRepository;
    private final FotoRepository fotoRepository;

    public ObraService(ObraRepository obraRepository,
            RegistroRepository registroRepository,
            FotoRepository fotoRepository) {
        this.obraRepository = obraRepository;
        this.registroRepository = registroRepository;
        this.fotoRepository = fotoRepository;
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ObraResponse> listar(StatusObra filtro) {
        return obraRepository.findAllByOrderByIdDesc().stream()
                .filter(o -> filtro == null || o.getStatus() == filtro)
                .map(this::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ObraResponse buscar(Long id) {
        return paraResposta(obter(id));
    }

    @Transactional(readOnly = true)
    public List<RegistroResponse> registros(Long obraId) {
        obter(obraId);
        return registroRepository.findByObraIdOrderByDataDescIdDesc(obraId).stream()
                .map(Mapeador::registro)
                .toList();
    }

    // ---------------------------------------------------------------
    // Cadastro / edição
    // ---------------------------------------------------------------

    @Transactional
    public ObraResponse criar(ObraRequest dados) {
        Obra obra = new Obra();
        aplicar(obra, dados);
        return paraResposta(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponse atualizar(Long id, ObraRequest dados) {
        Obra obra = obter(id);
        aplicar(obra, dados);
        return paraResposta(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponse finalizar(Long id) {
        Obra obra = obter(id);
        if (obra.isFinalizada()) {
            throw new NegocioException("Esta obra já está finalizada.");
        }
        obra.setStatus(StatusObra.FINALIZADA);
        obra.setDataFinalizacao(Datas.hoje());
        return paraResposta(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponse reabrir(Long id) {
        Obra obra = obter(id);
        if (!obra.isFinalizada()) {
            throw new NegocioException("Esta obra já está em andamento.");
        }
        obra.setStatus(StatusObra.EM_ANDAMENTO);
        obra.setDataFinalizacao(null);
        return paraResposta(obraRepository.save(obra));
    }

    /** Remove a obra com todos os registros e fotos (cascata). */
    @Transactional
    public void excluir(Long id) {
        obraRepository.delete(obter(id));
    }

    // ---------------------------------------------------------------
    // Internos
    // ---------------------------------------------------------------

    Obra obter(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Obra não encontrada."));
    }

    private void aplicar(Obra obra, ObraRequest d) {
        if (d.dataInicio() != null && d.previsaoTermino() != null
                && d.previsaoTermino().isBefore(d.dataInicio())) {
            throw new NegocioException("A previsão de término não pode ser anterior à data de início.");
        }
        obra.setNome(d.nome().trim());
        obra.setLocalizacao(limpar(d.localizacao()));
        obra.setResponsavel(limpar(d.responsavel()));
        obra.setDescricao(limpar(d.descricao()));
        obra.setDataInicio(d.dataInicio());
        obra.setPrevisaoTermino(d.previsaoTermino());
    }

    private static String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    ObraResponse paraResposta(Obra o) {
        Long id = o.getId();
        long total = registroRepository.countByObraId(id);

        LocalDate ultimo = registroRepository.findFirstByObraIdOrderByDataDescIdDesc(id)
                .map(Registro::getData).orElse(null);

        int progresso;
        if (o.isFinalizada()) {
            progresso = 100;
        } else {
            progresso = registroRepository
                    .findFirstByObraIdAndProgressoIsNotNullOrderByDataDescIdDesc(id)
                    .map(Registro::getProgresso).orElse(0);
        }

        String capa = fotoRepository.findFirstByRegistroObraIdOrderByIdDesc(id)
                .map(Mapeador::urlFoto).orElse(null);

        boolean atrasada = !o.isFinalizada()
                && o.getPrevisaoTermino() != null
                && o.getPrevisaoTermino().isBefore(Datas.hoje());

        return new ObraResponse(
                id, o.getNome(), o.getLocalizacao(), o.getResponsavel(), o.getDescricao(),
                o.getDataInicio(), o.getPrevisaoTermino(), o.getDataFinalizacao(),
                o.getStatus(), progresso, total, ultimo, capa, atrasada);
    }
}
