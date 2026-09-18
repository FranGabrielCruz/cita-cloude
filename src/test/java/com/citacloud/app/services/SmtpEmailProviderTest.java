package com.citacloud.app.services;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailProviderTest {

    @Test
    void usaConfiguracionTecnicaYNombreDeLaClinicaComoRemitente() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(mensaje);
        SmtpEmailProvider proveedor = new SmtpEmailProvider(sender, true, "smtp.example.com",
                "usuario@example.com", "clave#{%", "no-reply@example.com", true);

        assertTrue(proveedor.configurado());
        proveedor.enviar("paciente@example.com", "Cita confirmada", "Mensaje", "Clínica San Rafael");

        InternetAddress origen = (InternetAddress) mensaje.getFrom()[0];
        assertEquals("no-reply@example.com", origen.getAddress());
        assertEquals("Clínica San Rafael", origen.getPersonal());
        verify(sender).send(mensaje);
    }

    @Test
    void informaCadaDatoFaltanteSinExponerCredenciales() {
        SmtpEmailProvider proveedor = new SmtpEmailProvider(mock(JavaMailSender.class), false,
                "", "", "", "", true);

        assertEquals(5, proveedor.configuracionFaltante().size());
        assertTrue(proveedor.configuracionFaltante().contains("servidor SMTP (MAIL_HOST)"));
        assertTrue(proveedor.configuracionFaltante().contains("contraseña SMTP (MAIL_PASSWORD)"));
        assertTrue(proveedor.configuracionFaltante().contains("correo remitente (NOTIFICATIONS_EMAIL_FROM)"));
        assertTrue(proveedor.configuracionFaltante().contains("usuario SMTP (MAIL_USERNAME)"));
    }
}
