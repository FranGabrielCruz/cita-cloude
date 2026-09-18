package com.citacloud.app.services;

import com.citacloud.app.models.ConfiguracionNotificacionCita;
import com.citacloud.app.repositories.AuditoriaEventoRepository;
import com.citacloud.app.repositories.ConfiguracionNotificacionCitaRepository;
import com.citacloud.app.security.TenantUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfiguracionNotificacionCitaServiceTest {
    private final UUID empresaId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void autenticar() {
        TenantUserDetails usuario = new TenantUserDetails(usuarioId, empresaId, "CLINICA", "Clínica",
                "admin", "", "Admin", List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noHabilitaCorreoSinConfiguracionTecnica() {
        EmailProvider email = mock(EmailProvider.class);
        ConfiguracionNotificacionCitaService servicio = servicio(email, new ConfiguracionNotificacionCita());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(empresaId, true, ConfiguracionNotificacionCita.ASUNTO_PREDETERMINADO,
                        ConfiguracionNotificacionCita.CORREO_PREDETERMINADO));

        assertTrue(error.getMessage().contains("configuración técnica"));
    }

    @Test
    void guardaSoloCorreoYDeshabilitaWhatsapp() {
        EmailProvider email = mock(EmailProvider.class);
        when(email.configurado()).thenReturn(true);
        ConfiguracionNotificacionCita existente = new ConfiguracionNotificacionCita();
        existente.setEmpresaId(empresaId);
        existente.setSmtpFrom("anterior@clinica.com");
        existente.setSmtpUsername("usuario-anterior");
        existente.setWhatsappHabilitado(true);
        ConfiguracionNotificacionCitaService servicio = servicio(email, existente);

        ConfiguracionNotificacionCita guardada = servicio.guardar(empresaId, true,
                ConfiguracionNotificacionCita.ASUNTO_PREDETERMINADO,
                ConfiguracionNotificacionCita.CORREO_PREDETERMINADO);

        assertTrue(guardada.isCorreoHabilitado());
        assertFalse(guardada.isWhatsappHabilitado());
        assertNull(guardada.getSmtpFrom());
        assertNull(guardada.getSmtpUsername());
    }

    private ConfiguracionNotificacionCitaService servicio(EmailProvider email,
                                                           ConfiguracionNotificacionCita configuracion) {
        ConfiguracionNotificacionCitaRepository repositorio = mock(ConfiguracionNotificacionCitaRepository.class);
        when(repositorio.findByEmpresaIdAndEvento(any(), any())).thenReturn(Optional.of(configuracion));
        when(repositorio.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        AuditoriaService auditoria = new AuditoriaService(mock(AuditoriaEventoRepository.class), null);
        return new ConfiguracionNotificacionCitaService(repositorio,
                new PlantillaNotificacionService(), email, auditoria);
    }
}
