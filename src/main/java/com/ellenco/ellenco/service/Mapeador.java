package com.ellenco.ellenco.service;

import java.util.List;

import com.ellenco.ellenco.dto.FotoResponse;
import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.model.Foto;
import com.ellenco.ellenco.model.Registro;

/** Conversões entidade -> DTO (chamar sempre dentro de uma transação). */
final class Mapeador {

    private Mapeador() {
    }

    static String urlFoto(Foto f) {
        String caminho = f.getCaminho();
        // fotos do modelo antigo (gravadas em disco)
        if (caminho != null && caminho.startsWith("/uploads/")) {
            return caminho;
        }
        return "/api/fotos/" + f.getId();
    }

    static FotoResponse foto(Foto f) {
        return new FotoResponse(f.getId(), urlFoto(f), f.getNomeArquivo());
    }

    static RegistroResponse registro(Registro r) {
        List<FotoResponse> fotos = r.getFotos().stream().map(Mapeador::foto).toList();
        return new RegistroResponse(
                r.getId(),
                r.getObra().getId(),
                r.getObra().getNome(),
                r.getData(),
                r.getHorario(),
                r.getTurno(),
                r.getClima(),
                r.getDescricao(),
                r.getLatitude(),
                r.getLongitude(),
                r.getProgresso(),
                r.getAutor(),
                fotos);
    }
}
