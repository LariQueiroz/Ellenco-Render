package com.ellenco.ellenco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record CadastroRequest(
            @NotBlank(message = "Informe o nome completo.") @Size(max = 255) String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 255) String email,
            @NotBlank(message = "Informe o login.") @Size(min = 3, max = 50, message = "O login deve ter de 3 a 50 caracteres.") String login,
            @NotBlank(message = "Informe a senha.") @Size(min = 6, max = 100, message = "A senha deve possuir pelo menos 6 caracteres.") String senha,
            @NotBlank(message = "Selecione o cargo.") @Size(max = 100) String cargo) {
    }

    public record LoginRequest(String login, String senha) {
    }

    public record PerfilRequest(
            @NotBlank(message = "Informe o nome.") @Size(max = 255) String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 255) String email,
            @Size(max = 100) String cargo) {
    }

    public record SenhaRequest(
            @NotBlank(message = "Informe a nova senha.") @Size(min = 6, max = 100, message = "A senha deve possuir pelo menos 6 caracteres.") String novaSenha) {
    }

    public record RecuperacaoRequest(String email) {
    }

    public record RedefinirSenhaRequest(
            String token,
            @NotBlank(message = "Informe a nova senha.") @Size(min = 6, max = 100, message = "A senha deve possuir pelo menos 6 caracteres.") String novaSenha) {
    }
}
