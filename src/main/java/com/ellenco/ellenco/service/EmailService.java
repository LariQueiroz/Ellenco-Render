package com.ellenco.ellenco.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacao(String email, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(email);
        mensagem.setSubject("Recuperação de senha - Ellenco");
        mensagem.setText(
                "Olá!\n\n"
                + "Recebemos uma solicitação para recuperação da sua senha.\n\n"
                + "Clique no link abaixo para criar uma nova senha (válido por 30 minutos):\n\n"
                + link
                + "\n\n"
                + "Se você não solicitou essa alteração, ignore este e-mail.");
        mailSender.send(mensagem);
    }
}
