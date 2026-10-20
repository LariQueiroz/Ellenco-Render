package com.ellenco.ellenco.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import com.ellenco.ellenco.dto.AuthDtos.*;
import com.ellenco.ellenco.dto.UsuarioResponse;
import com.ellenco.ellenco.exception.NaoEncontradoException;
import com.ellenco.ellenco.model.Usuario;
import com.ellenco.ellenco.service.EmailService;
import com.ellenco.ellenco.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UsuarioController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    // ---------------------------------------------------------------
    // Cadastro / login / logout
    // ---------------------------------------------------------------

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest dados) {
        Usuario novo = usuarioService.cadastrar(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(novo));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dados,
            HttpServletRequest request, HttpServletResponse response) {

        Usuario usuario = usuarioService.autenticar(dados.login(), dados.senha()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Login ou senha incorretos."));
        }

        // evita fixação de sessão e grava a autenticação na sessão HTTP
        HttpSession sessaoAntiga = request.getSession(false);
        if (sessaoAntiga != null) {
            request.changeSessionId();
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                usuario.getLogin(), null, List.of(new SimpleGrantedAuthority("ROLE_USUARIO"))));
        SecurityContextHolder.setContext(contexto);
        contextRepository.saveContext(contexto, request, response);

        return ResponseEntity.ok(UsuarioResponse.de(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Usuário logado
    // ---------------------------------------------------------------

    @GetMapping("/me")
    public UsuarioResponse me(Authentication auth) {
        return UsuarioResponse.de(logado(auth));
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarPerfil(Authentication auth, @Valid @RequestBody PerfilRequest dados) {
        return UsuarioResponse.de(usuarioService.atualizarPerfil(auth.getName(), dados));
    }

    @PutMapping("/me/senha")
    public Map<String, String> alterarSenha(Authentication auth, @Valid @RequestBody SenhaRequest dados) {
        usuarioService.alterarSenha(auth.getName(), dados.novaSenha());
        return Map.of("message", "Senha alterada com sucesso.");
    }

    // ---------------------------------------------------------------
    // Recuperação de senha
    // ---------------------------------------------------------------

    @PostMapping("/esqueci-senha")
    public Map<String, String> esqueciSenha(@RequestBody RecuperacaoRequest dados) {
        usuarioService.gerarTokenRecuperacao(dados.email()).ifPresent(usuario -> {
            String link = baseUrl + "/redefinir-senha.html?token=" + usuario.getTokenRecuperacao();
            try {
                //EmailService..enviarEmailRecuperacao(usuario.getEmail(), link);
            } catch (Exception e) {
                // não revela ao cliente se o e-mail existe ou se o envio falhou
                log.error("Falha ao enviar e-mail de recuperação para o usuário {}", usuario.getId(), e);
            }
        });
        return Map.of("message",
                "Se os dados estiverem cadastrados, as instruções de recuperação foram enviadas por e-mail.");
    }

    @PostMapping("/redefinir-senha")
    public Map<String, String> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest dados) {
        usuarioService.redefinirSenha(dados.token(), dados.novaSenha());
        return Map.of("message", "Senha alterada com sucesso.");
    }

    private Usuario logado(Authentication auth) {
        return usuarioService.buscarPorLogin(auth.getName())
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }
}
