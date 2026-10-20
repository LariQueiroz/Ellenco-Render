package com.ellenco.ellenco.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ellenco.ellenco.dto.AuthDtos.CadastroRequest;
import com.ellenco.ellenco.dto.AuthDtos.PerfilRequest;
import com.ellenco.ellenco.exception.NaoEncontradoException;
import com.ellenco.ellenco.exception.NegocioException;
import com.ellenco.ellenco.model.Usuario;
import com.ellenco.ellenco.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final long VALIDADE_TOKEN_MS = 30L * 60L * 1000L;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase();
        String login = dados.login().trim();

        if (usuarioRepository.existsByEmail(email)) {
            throw new NegocioException("E-mail já cadastrado.");
        }
        if (usuarioRepository.existsByLogin(login)) {
            throw new NegocioException("Login já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setLogin(login);
        usuario.setCargo(dados.cargo().trim());
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        return usuarioRepository.save(usuario);
    }

    /** Aceita login ou e-mail. Retorna vazio se as credenciais não conferem. */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String identificador, String senha) {
        if (identificador == null || senha == null || identificador.isBlank()) {
            return Optional.empty();
        }
        String id = identificador.trim();
        Optional<Usuario> usuario = usuarioRepository.findByLogin(id);
        if (usuario.isEmpty()) {
            usuario = usuarioRepository.findByEmail(id.toLowerCase());
        }
        return usuario.filter(u -> passwordEncoder.matches(senha, u.getSenha()));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    @Transactional
    public Usuario atualizarPerfil(String login, PerfilRequest dados) {
        Usuario usuario = obterPorLogin(login);
        String email = dados.email().trim().toLowerCase();

        usuarioRepository.findByEmail(email).ifPresent(outro -> {
            if (!outro.getId().equals(usuario.getId())) {
                throw new NegocioException("E-mail já cadastrado.");
            }
        });

        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setCargo(dados.cargo() == null ? null : dados.cargo().trim());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenha(String login, String novaSenha) {
        Usuario usuario = obterPorLogin(login);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setTokenRecuperacao(null);
        usuario.setTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Gera o token de recuperação para o usuário que tiver este e-mail ou login.
     * Retorna vazio quando não existe (o controller responde igual nos dois casos).
     */
    @Transactional
    public Optional<Usuario> gerarTokenRecuperacao(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        String id = identificador.trim();
        Optional<Usuario> usuario = usuarioRepository.findByEmail(id.toLowerCase());
        if (usuario.isEmpty()) {
            usuario = usuarioRepository.findByLogin(id);
        }
        usuario.ifPresent(u -> {
            u.setTokenRecuperacao(UUID.randomUUID().toString());
            u.setTokenExpiracao(System.currentTimeMillis() + VALIDADE_TOKEN_MS);
            usuarioRepository.save(u);
        });
        return usuario;
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new NegocioException("Token inválido.");
        }
        Usuario usuario = usuarioRepository.findByTokenRecuperacao(token)
                .orElseThrow(() -> new NegocioException("Token inválido."));

        if (usuario.getTokenExpiracao() == null
                || System.currentTimeMillis() > usuario.getTokenExpiracao()) {
            throw new NegocioException("Token expirado. Solicite uma nova recuperação de senha.");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setTokenRecuperacao(null);
        usuario.setTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }

    private Usuario obterPorLogin(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }
}
