package com.ellenco.ellenco.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ellenco.ellenco.dto.RegistroInput;
import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.exception.NaoEncontradoException;
import com.ellenco.ellenco.exception.NegocioException;
import com.ellenco.ellenco.model.Foto;
import com.ellenco.ellenco.model.FotoConteudo;
import com.ellenco.ellenco.model.Obra;
import com.ellenco.ellenco.model.Registro;
import com.ellenco.ellenco.repository.FotoRepository;
import com.ellenco.ellenco.repository.ObraRepository;
import com.ellenco.ellenco.repository.RegistroRepository;
import com.ellenco.ellenco.util.Datas;

@Service
public class RegistroService {

    private static final int MAX_FOTOS = 20;

    private final RegistroRepository registroRepository;
    private final ObraRepository obraRepository;
    private final FotoRepository fotoRepository;

    public RegistroService(RegistroRepository registroRepository,
            ObraRepository obraRepository,
            FotoRepository fotoRepository) {
        this.registroRepository = registroRepository;
        this.obraRepository = obraRepository;
        this.fotoRepository = fotoRepository;
    }

    @Transactional
    public RegistroResponse criar(Long obraId, RegistroInput dados, MultipartFile[] fotos, String autor)
            throws IOException {

        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new NaoEncontradoException("Obra não encontrada."));

        if (obra.isFinalizada()) {
            throw new NegocioException(
                    "Esta obra está finalizada. Reabra a obra para lançar novos registros.");
        }

        validar(dados);

        MultipartFile[] arquivos = fotos == null ? new MultipartFile[0] : fotos;
        int quantidade = 0;
        for (MultipartFile f : arquivos) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            String tipo = f.getContentType();
            if (tipo == null || !tipo.toLowerCase().startsWith("image/")) {
                throw new NegocioException("Apenas imagens são aceitas (arquivo: " + nomeSeguro(f) + ").");
            }
            quantidade++;
        }
        if (quantidade > MAX_FOTOS) {
            throw new NegocioException("Envie no máximo " + MAX_FOTOS + " fotos por registro.");
        }

        Registro registro = new Registro();
        registro.setObra(obra);
        registro.setData(dados.data());
        registro.setHorario(dados.horario());
        registro.setTurno(dados.turno().trim());
        registro.setClima(dados.clima().trim());
        registro.setDescricao(dados.descricao().trim());
        registro.setLatitude(dados.latitude());
        registro.setLongitude(dados.longitude());
        registro.setProgresso(dados.progresso());
        registro.setAutor(autor);

        for (MultipartFile f : arquivos) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            Foto foto = new Foto();
            foto.setRegistro(registro);
            foto.setCaminho("pendente"); // ajustado para /api/fotos/{id} logo abaixo
            foto.setNomeArquivo(nomeSeguro(f));
            foto.setTipoConteudo(f.getContentType());
            foto.setTamanho(f.getSize());
            foto.setConteudo(new FotoConteudo(f.getBytes()));
            registro.getFotos().add(foto);
        }

        Registro salvo = registroRepository.saveAndFlush(registro);
        for (Foto foto : salvo.getFotos()) {
            foto.setCaminho("/api/fotos/" + foto.getId());
        }

        return Mapeador.registro(salvo);
    }

    @Transactional(readOnly = true)
    public RegistroResponse buscar(Long id) {
        return Mapeador.registro(obter(id));
    }

    @Transactional(readOnly = true)
    public java.util.List<RegistroResponse> listarPorObra(Long obraId) {
        if (!obraRepository.existsById(obraId)) {
            throw new NaoEncontradoException("Obra não encontrada.");
        }
        return registroRepository.findByObraIdOrderByDataDescIdDesc(obraId).stream()
                .map(Mapeador::registro).toList();
    }

    @Transactional
    public void excluir(Long id) {
        registroRepository.delete(obter(id));
    }

    /** Dados da imagem para o endpoint /api/fotos/{id}. */
    @Transactional(readOnly = true)
    public ImagemArmazenada imagem(Long fotoId) {
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new NaoEncontradoException("Foto não encontrada."));
        if (foto.getConteudo() == null) {
            throw new NaoEncontradoException("Foto não encontrada.");
        }
        return new ImagemArmazenada(foto.getTipoConteudo(), foto.getConteudo().getDados());
    }

    public record ImagemArmazenada(String tipo, byte[] dados) {
    }

    // ---------------------------------------------------------------

    private Registro obter(Long id) {
        return registroRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Registro não encontrado."));
    }

    private void validar(RegistroInput d) {
        if (d.data() == null) {
            throw new NegocioException("Informe a data.");
        }
        if (d.data().isAfter(Datas.hoje())) {
            throw new NegocioException("A data do registro não pode ser futura.");
        }
        if (d.turno() == null || d.turno().isBlank()) {
            throw new NegocioException("Selecione o turno.");
        }
        if (d.clima() == null || d.clima().isBlank()) {
            throw new NegocioException("Selecione o clima.");
        }
        if (d.descricao() == null || d.descricao().isBlank()) {
            throw new NegocioException("Digite o relato do dia.");
        }
        if (d.progresso() != null && (d.progresso() < 0 || d.progresso() > 100)) {
            throw new NegocioException("O progresso deve estar entre 0 e 100.");
        }
        if ((d.latitude() == null) != (d.longitude() == null)) {
            throw new NegocioException("Informe latitude e longitude juntas.");
        }
        if (d.latitude() != null
                && (d.latitude() < -90 || d.latitude() > 90 || d.longitude() < -180 || d.longitude() > 180)) {
            throw new NegocioException("Coordenadas inválidas.");
        }
    }

    private static String nomeSeguro(MultipartFile f) {
        String nome = f.getOriginalFilename();
        if (nome == null || nome.isBlank()) {
            return "foto";
        }
        // remove qualquer caminho enviado pelo cliente
        nome = nome.replace("\\", "/");
        nome = nome.substring(nome.lastIndexOf('/') + 1);
        return nome.length() > 200 ? nome.substring(nome.length() - 200) : nome;
    }
}
