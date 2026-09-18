package com.citacloud.app.services;

import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificacionCitaOutboxService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", new Locale("es", "DO"));
    private final ConfiguracionNotificacionCitaService config;
    private final PlantillaNotificacionService plantillas;
    private final EntregaNotificacionRepository entregas;
    private final OutboxNotificacionRepository outbox;
    private final EmpresaRepository empresas;
    private final MetricasNotificaciones metricas;

    public NotificacionCitaOutboxService(ConfiguracionNotificacionCitaService config,
                                        PlantillaNotificacionService plantillas,
                                        EntregaNotificacionRepository entregas,
                                        OutboxNotificacionRepository outbox,
                                        EmpresaRepository empresas) {
        this(config, plantillas, entregas, outbox, empresas, null);
    }

    @Autowired
    public NotificacionCitaOutboxService(ConfiguracionNotificacionCitaService config,
                                        PlantillaNotificacionService plantillas,
                                        EntregaNotificacionRepository entregas,
                                        OutboxNotificacionRepository outbox,
                                        EmpresaRepository empresas,
                                        MetricasNotificaciones metricas) {
        this.config = config;
        this.plantillas = plantillas;
        this.entregas = entregas;
        this.outbox = outbox;
        this.empresas = empresas;
        this.metricas = metricas;
    }

    @Transactional
    public void registrarCitaCreada(Cita cita) {
        if (cita == null || cita.getId() == null) return;
        ConfiguracionNotificacionCita configuracion = config.obtener(cita.getEmpresaId());
        Map<String, String> valores = valores(cita);
        if (configuracion.isCorreoHabilitado()) {
            crear(cita, "EMAIL", emailValido(cita.getPaciente().getEmail()),
                    plantillas.renderizar(configuracion.getAsuntoCorreo(), valores),
                    plantillas.renderizar(configuracion.getMensajeCorreo(), valores), null);
        }
        if (configuracion.isWhatsappHabilitado()) {
            crear(cita, "WHATSAPP", normalizarTelefono(cita.getPaciente().getTelefono()), null,
                    plantillas.renderizar(configuracion.getMensajeWhatsapp(), valores),
                    configuracion.getWhatsappTemplateId());
        }
    }

    private void crear(Cita cita, String canal, String destino, String asunto, String mensaje, String templateId) {
        if (entregas.existsByEmpresaIdAndEntidadIdAndEventoAndCanal(cita.getEmpresaId(), cita.getId(),
                ConfiguracionNotificacionCita.EVENTO_CITA_CREADA, canal)) return;
        EntregaNotificacion entrega = new EntregaNotificacion();
        entrega.setEmpresaId(cita.getEmpresaId());
        entrega.setEvento(ConfiguracionNotificacionCita.EVENTO_CITA_CREADA);
        entrega.setEntidadTipo("APPOINTMENT");
        entrega.setEntidadId(cita.getId());
        entrega.setCanal(canal);
        entrega.setAsunto(asunto);
        entrega.setMensajeRenderizado(mensaje);
        entrega.setProveedor("EMAIL".equals(canal) ? "SMTP" : "WHATSAPP_HTTP");
        if (destino == null) {
            entrega.setEstado("OMITIDO");
            entrega.setCodigoFallo("EMAIL".equals(canal) ? "PATIENT_EMAIL_MISSING" : "PATIENT_PHONE_MISSING");
            entrega.setMotivoFallo("No existe un dato de contacto válido para este canal.");
        } else {
            entrega.setDestinatario(destino);
            entrega.setDestinatarioEnmascarado(enmascarar(destino, canal));
            entrega.setEstado("PENDIENTE");
        }
        try {
            entrega = entregas.save(entrega);
            if (destino == null) {
                if (metricas != null) metricas.omitido(canal);
            } else {
                OutboxNotificacion item = new OutboxNotificacion();
                item.setEmpresaId(cita.getEmpresaId());
                item.setEntrega(entrega);
                outbox.save(item);
            }
        } catch (DataIntegrityViolationException ignored) {
            // La restricción idempotente evita dobles envíos y dobles conteos.
        }
    }

    private Map<String, String> valores(Cita cita) {
        String clinica = empresas.findById(cita.getEmpresaId()).map(Empresa::getNombre).orElse("Clínica");
        return Map.of("paciente", cita.getPaciente().getNombreCompleto(),
                "fecha_cita", cita.getFecha().format(FECHA), "hora_cita", cita.getHoraInicio().format(HORA),
                "medico", cita.getMedico().getNombreCompleto(),
                "especialidad", cita.getMedico().getEspecialidadesTexto(),
                "sucursal", cita.getSucursal().getNombre(),
                "consultorio", cita.getConsultorio() == null ? "—" : cita.getConsultorio().getNombre(),
                "clinica", clinica);
    }

    String normalizarTelefono(String telefono) {
        if (telefono == null) return null;
        String digitos = telefono.trim().replaceAll("\\D", "");
        if (digitos.length() == 10) digitos = "1" + digitos;
        if (digitos.length() < 11 || digitos.length() > 15) return null;
        return "+" + digitos;
    }

    private String emailValido(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") ? email.trim() : null;
    }

    private String enmascarar(String destino, String canal) {
        if ("EMAIL".equals(canal)) {
            int indice = destino.indexOf('@');
            return indice < 1 ? "***" : destino.substring(0, 1) + "***" + destino.substring(indice);
        }
        return "***-***-" + destino.substring(Math.max(0, destino.length() - 4));
    }
}
