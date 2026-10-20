package com.ellenco.ellenco.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ellenco.ellenco.dto.RegistroInput;
import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.exception.NegocioException;
import com.ellenco.ellenco.model.Usuario;
import com.ellenco.ellenco.service.RegistroService;
import com.ellenco.ellenco.service.UsuarioService;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    private final RegistroService registroService;
    private final UsuarioService usuarioService;

    public RegistroController(RegistroService registroService, UsuarioService usuarioService) {
        this.registroService = registroService;
        this.usuarioService = usuarioService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistroResponse> criar(
            Authentication auth,
            @RequestParam Long obraId,
            @RequestParam String data,
            @RequestParam(required = false) String horario,
            @RequestParam String turno,
            @RequestParam String clima,
            @RequestParam String descricao,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer progresso,
            @RequestParam(required = false) MultipartFile[] fotos) throws IOException {

        LocalDate dataRegistro;
        LocalTime horarioRegistro = null;
        try {
            dataRegistro = LocalDate.parse(data);
            if (horario != null && !horario.isBlank()) {
                horarioRegistro = LocalTime.parse(horario);
            }
        } catch (DateTimeParseException e) {
            throw new NegocioException("Data ou horário inválido.");
        }

        String autor = usuarioService.buscarPorLogin(auth.getName())
                .map(Usuario::getNome).orElse(auth.getName());

        RegistroInput entrada = new RegistroInput(
                dataRegistro, horarioRegistro, turno, clima, descricao, latitude, longitude, progresso);

        RegistroResponse salvo = registroService.criar(obraId, entrada, fotos, autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/obra/{obraId}")
    public List<RegistroResponse> listarPorObra(@PathVariable Long obraId) {
        return registroService.listarPorObra(obraId);
    }

    @GetMapping("/{id}")
    public RegistroResponse buscar(@PathVariable Long id) {
        return registroService.buscar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        registroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
