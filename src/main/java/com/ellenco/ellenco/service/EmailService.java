package com.ellenco.ellenco.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestClient.Builder restClientBuilder = null;

    @Value("${marketmax.mail.from}")
    private String fromAddress;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${marketmax.app.base-url}")
    private String appBaseUrl;

    @Value("${marketmax.reset-token.expiration-minutes}")
    private long expirationMinutes;

    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        String resetLink = appBaseUrl + "/redefinir-senha.html?token=" + token;

        String htmlContent = "<p>Olá, <strong>" + userName + "</strong>!</p>" +
                "<p>Recebemos uma solicitação para redefinir a senha da sua conta MarketMax.</p>" +
                "<p><a href=\"" + resetLink + "\" style=\"padding: 10px 15px; background: #1976D2; color: white; text-decoration: none; border-radius: 5px;\">Redefinir Senha</a></p>" +
                "<p>Este link é válido por " + expirationMinutes + " minutos.</p>" +
                "<p>Se você não solicitou, apenas ignore este e-mail.</p>" +
                "<p>Equipe MarketMax</p>";

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl("https://api.resend.com")
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "from", fromAddress,
                    "to", new String[]{toEmail},
                    "subject", "MarketMax — Redefinição de senha",
                    "html", htmlContent
            );

            restClient.post()
                    .uri("/emails")
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

           
        } catch (Exception ex) {
           
            throw ex;
        }
    }
}