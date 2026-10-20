package com.ellenco.ellenco.controller;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ellenco.ellenco.service.RegistroService;
import com.ellenco.ellenco.service.RegistroService.ImagemArmazenada;

@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final RegistroService registroService;

    public FotoController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id) {
        ImagemArmazenada imagem = registroService.imagem(id);

        MediaType tipo;
        try {
            tipo = MediaType.parseMediaType(imagem.tipo());
        } catch (Exception e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(tipo)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(imagem.dados());
    }
}
