package com.oshayer.event_manager.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${app.api-base-url}")
    private String apiBaseUrl;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your email";
        String verificationUrl = buildTokenUrl("/api/auth/verify", token);
        String body = "Click the link to verify your email: " + verificationUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset your password";
        String resetUrl = buildTokenUrl("/api/auth/reset-password", token);
        String body = "Click the link to reset your password: " + resetUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildTokenUrl(String path, String token) {
        return UriComponentsBuilder.fromHttpUrl(apiBaseUrl)
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }
}
