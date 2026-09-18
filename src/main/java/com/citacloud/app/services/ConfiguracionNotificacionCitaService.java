package com.citacloud.app.services;

import com.citacloud.app.models.ConfiguracionNotificacionCita;
import com.citacloud.app.repositories.ConfiguracionNotificacionCitaRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ConfiguracionNotificacionCitaService {
    private final ConfiguracionNotificacionCitaRepository repositorio;
    private final PlantillaNotificacionService plantillas;
    private final EmailProvider email;
    private final AuditoriaService auditoria;

    public ConfiguracionNotificacionCitaService(ConfiguracionNotificacionCitaRepository repositorio,
                                                PlantillaNotificacionService plantillas,
                                                EmailProvider email,
                                                AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.plantillas = plantillas;
        this.email = email;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public ConfiguracionNotificacionCita obtener(UUID empresaId) {
        return repositorio.findByEmpresaIdAndEvento(empresaId, ConfiguracionNotificacionCita.EVENTO_CITA_CREADA)
                .orElseGet(() -> {
                    ConfiguracionNotificacionCita configuracion = new ConfiguracionNotificacionCita();
                    configuracion.setEmpresaId(empresaId);
                    return configuracion;
                });
    }

    @Transactional
    public ConfiguracionNotificacionCita guardar(UUID empresaId, boolean correoHabilitado,
                                                  String asunto, String cuerpo) {
        autorizar(empresaId);
        plantillas.validar(asunto);
        plantillas.validar(cuerpo);
        if (correoHabilitado && !email.configurado()) {
            throw new IllegalArgumentException(errorConfiguracionCorreo());
        }

        ConfiguracionNotificacionCita configuracion = obtener(empresaId);
        boolean nueva = configuracion.getId() == null;
        List<AuditoriaService.Cambio> cambios = List.of(
                new AuditoriaService.Cambio("correo_habilitado",
                        String.valueOf(configuracion.isCorreoHabilitado()), String.valueOf(correoHabilitado)),
                new AuditoriaService.Cambio("whatsapp_habilitado",
                        String.valueOf(configuracion.isWhatsappHabilitado()), "false"),
                new AuditoriaService.Cambio("smtp_from", configuracion.getSmtpFrom(), "[CONFIGURACIÓN TÉCNICA]"),
                new AuditoriaService.Cambio("smtp_username", configuracion.getSmtpUsername(), "[CONFIGURACIÓN TÉCNICA]"),
                new AuditoriaService.Cambio("asunto_correo", resumen(configuracion.getAsuntoCorreo()), resumen(asunto)),
                new AuditoriaService.Cambio("plantilla_correo", "[PLANTILLA]", "[PLANTILLA ACTUALIZADA]"));

        configuracion.setCorreoHabilitado(correoHabilitado);
        configuracion.setSmtpFrom(null);
        configuracion.setSmtpUsername(null);
        configuracion.setAsuntoCorreo(asunto.trim());
        configuracion.setMensajeCorreo(cuerpo.trim());
        // WhatsApp queda temporalmente fuera de Configuración y no debe seguir enviando en segundo plano.
        configuracion.setWhatsappHabilitado(false);
        configuracion.setActualizadoEn(LocalDateTime.now());
        configuracion.setActualizadoPor(usuario().getUsuarioId());

        ConfiguracionNotificacionCita guardada = repositorio.save(configuracion);
        auditoria.registrar(empresaId, null, "CONFIGURACION",
                "APPOINTMENT_NOTIFICATION_SETTINGS_UPDATED", "CONFIGURACION_NOTIFICACION_CITA",
                guardada.getId(), nueva ? "Configuración creada" : "Configuración actualizada",
                null, cambios, "SUCCESS", null, false);
        return guardada;
    }

    public boolean correoConfigurado() {
        return email.configurado();
    }

    public String vistaPrevia(String plantilla) {
        return plantillas.renderizar(plantilla, plantillas.ejemplo());
    }

    private String errorConfiguracionCorreo() {
        List<String> faltantes = email.configuracionFaltante();
        if (faltantes == null || faltantes.isEmpty()) {
            return "No es posible habilitar el envío de correo porque la configuración técnica está incompleta.";
        }
        return "No es posible habilitar el envío de correo. Falta: " + String.join(", ", faltantes) + ".";
    }

    private void autorizar(UUID empresaId) {
        TenantUserDetails usuario = usuario();
        if (empresaId == null || !empresaId.equals(usuario.getEmpresaId())) {
            throw new IllegalArgumentException("No tiene acceso a la configuración de otra empresa.");
        }
        boolean permitido = usuario.getAuthorities().stream()
                .anyMatch(authority -> Set.of("ROLE_ADMINISTRADOR", "ROLE_SUPERADMIN",
                        "APPOINTMENT_NOTIFICATION_SETTINGS_EDIT").contains(authority.getAuthority()));
        if (!permitido) {
            throw new IllegalArgumentException("No tiene permiso para modificar las notificaciones de citas.");
        }
    }

    private TenantUserDetails usuario() {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null) throw new IllegalArgumentException("La sesión no es válida.");
        return usuario;
    }

    private String resumen(String valor) {
        return valor == null ? null : valor.length() > 120 ? valor.substring(0, 120) : valor;
    }
}
