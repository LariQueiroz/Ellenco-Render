package com.ellenco.ellenco.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ellenco.ellenco.dto.ObraRequest;
import com.ellenco.ellenco.dto.ObraResponse;
import com.ellenco.ellenco.dto.RegistroResponse;
import com.ellenco.ellenco.dto.RelatorioSemanalResponse;
import com.ellenco.ellenco.model.StatusObra;
import com.ellenco.ellenco.service.ObraService;
import com.ellenco.ellenco.service.RelatorioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/obras")
public class ObraController {

    private final ObraService obraService;
    private final RelatorioService relatorioService;

    public ObraController(ObraService obraService, RelatorioService relatorioService) {
        this.obraService = obraService;
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public List<ObraResponse> listar(@RequestParam(required = false) StatusObra status) {
        return obraService.listar(status);
    }

    @GetMapping("/{id}")
    public ObraResponse buscar(@PathVariable Long id) {
        return obraService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ObraResponse> cadastrar(@Valid @RequestBody ObraRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(obraService.criar(dados));
    }

    @PutMapping("/{id}")
    public ObraResponse atualizar(@PathVariable Long id, @Valid @RequestBody ObraRequest dados) {
        return obraService.atualizar(id, dados);
    }

    @PostMapping("/{id}/finalizar")
    public ObraResponse finalizar(@PathVariable Long id) {
        return obraService.finalizar(id);
    }

    @PostMapping("/{id}/reabrir")
    public ObraResponse reabrir(@PathVariable Long id) {
        return obraService.reabrir(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        obraService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Registros da obra (1 obra : N registros), do mais recente para o mais antigo. */
    @GetMapping("/{id}/registros")
    public List<RegistroResponse> registros(@PathVariable Long id) {
        return obraService.registros(id);
    }

    /** Relatório semanal; "inicio" é qualquer data da semana desejada (padrão: semana atual). */
    @GetMapping("/{id}/relatorio-semanal")
    public RelatorioSemanalResponse relatorioSemanal(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio) {
        return relatorioService.semanal(id, inicio);
    }
}
