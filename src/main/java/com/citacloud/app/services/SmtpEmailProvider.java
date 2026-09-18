package com.citacloud.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SmtpEmailProvider implements EmailProvider {
    private final boolean habilitado, autenticacion;
    private final String host, usuario, password, remitente;
    private final JavaMailSender sender;

    public SmtpEmailProvider(JavaMailSender sender,
                             @Value("${app.notifications.email.configured:false}") boolean habilitado,
                             @Value("${spring.mail.host:}") String host,
                             @Value("${spring.mail.username:}") String usuario,
                             @Value("${spring.mail.password:}") String password,
                             @Value("${app.notifications.email.from:}") String remitente,
                             @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean autenticacion) {
        this.sender = sender;
        this.habilitado = habilitado;
        this.host = host;
        this.usuario = usuario;
        this.password = password;
        this.remitente = remitente;
        this.autenticacion = autenticacion;
    }

    @Override public boolean configurado() {
        return configuracionFaltante().isEmpty();
    }

    @Override public List<String> configuracionFaltante() {
        List<String> faltantes = new ArrayList<>();
        if (!habilitado) faltantes.add("habilitación técnica (NOTIFICATIONS_EMAIL_CONFIGURED=true)");
        if (vacio(host)) faltantes.add("servidor SMTP (MAIL_HOST)");
        if (autenticacion && vacio(password)) faltantes.add("contraseña SMTP (MAIL_PASSWORD)");
        if (vacio(remitente)) faltantes.add("correo remitente (NOTIFICATIONS_EMAIL_FROM)");
        if (vacio(usuario)) faltantes.add("usuario SMTP (MAIL_USERNAME)");
        return List.copyOf(faltantes);
    }

    @Override
    public Resultado enviar(String destinatario, String asunto, String mensaje, String nombreClinica) {
        if (!configurado()) throw new IllegalStateException("EMAIL_CHANNEL_NOT_CONFIGURED");
        try {
            MimeMessage correo = sender.createMimeMessage();
            MimeMessageHelper contenido = new MimeMessageHelper(correo, StandardCharsets.UTF_8.name());
            contenido.setFrom(remitente, nombreRemitente(nombreClinica));
            contenido.setTo(destinatario);
            contenido.setSubject(asunto);
            contenido.setText(mensaje, false);
            sender.send(correo);
            return new Resultado("SMTP", null);
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new MailPreparationException("No fue posible preparar el correo SMTP.", exception);
        }
    }

    private String nombreRemitente(String nombreClinica) {
        return vacio(nombreClinica) ? "CitaCloud" : nombreClinica.trim();
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}
