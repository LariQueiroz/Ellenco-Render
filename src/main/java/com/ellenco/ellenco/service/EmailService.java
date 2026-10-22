package com.ellenco.ellenco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {

    private final RestClient resendClient;
    private final String fromAddress;
    private final String appBaseUrl;

    public EmailService(
            @Value("${resend.api.key}") String resendApiKey,
            @Value("${ellenco.from.email}") String fromAddress,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
            @Value("${resend.api.url:https://api.resend.com/emails}") String resendApiUrl) {
        this.fromAddress = fromAddress;
        this.appBaseUrl = appBaseUrl.replaceAll("/+$", "");
        this.resendClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + resendApiKey)
                .build();
        this.resendApiUrl = resendApiUrl;
    }

    private final String resendApiUrl;

    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        String resetLink = appBaseUrl + "/redefinir-senha.html?token=" + token;
        String safeName = userName == null ? "usuário" : userName
                .replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");

        String htmlContent = "<p>Olá, <strong>" + safeName + "</strong>!</p>" +
                "<p>Recebemos uma solicitação para redefinir a senha da sua conta Ellenco.</p>" +
                "<p><a href=\"" + resetLink + "\" style=\"padding: 10px 15px; background: #1976D2; color: white; text-decoration: none; border-radius: 5px;\">Redefinir Senha</a></p>" +
                "<p>Este link é válido por 30 minutos.</p>" +
                "<p>Se você não solicitou, apenas ignore este e-mail.</p>" +
                "<p>Equipe Ellenco</p>";

        resendClient.post()
                .uri(resendApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", fromAddress,
                        "to", new String[]{toEmail},
                        "subject", "Ellenco — Redefinição de senha",
                        "html", htmlContent))
                .retrieve()
                .toBodilessEntity();
    }
}
