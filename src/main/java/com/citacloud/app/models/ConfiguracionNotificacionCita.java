package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_notificaciones_cita", uniqueConstraints =
        @UniqueConstraint(columnNames = {"empresa_id", "evento"}))
public class ConfiguracionNotificacionCita {
    public static final String EVENTO_CITA_CREADA = "APPOINTMENT_CREATED";
    public static final String ASUNTO_PREDETERMINADO = "Confirmación de cita - {{clinica}}";
    public static final String CORREO_PREDETERMINADO = "Hola {{paciente}},\n\nSu cita ha sido programada correctamente.\n\nFecha: {{fecha_cita}}\nHora: {{hora_cita}}\nMédico: {{medico}}\nEspecialidad: {{especialidad}}\nSucursal: {{sucursal}}\nConsultorio: {{consultorio}}\n\nGracias,\n{{clinica}}";
    public static final String WHATSAPP_PREDETERMINADO = "Hola {{paciente}}, su cita en {{clinica}} ha sido programada para el {{fecha_cita}} a las {{hora_cita}} con {{medico}}.\n\nSucursal: {{sucursal}}.";

    @Id @GeneratedValue private UUID id;
    @Column(name="empresa_id", nullable=false) private UUID empresaId;
    @Column(nullable=false, length=50) private String evento = EVENTO_CITA_CREADA;
    @Column(name="correo_habilitado", nullable=false) private boolean correoHabilitado;
    @Column(name="smtp_from") private String smtpFrom;
    @Column(name="smtp_username") private String smtpUsername;
    @Column(name="asunto_correo", nullable=false, columnDefinition="TEXT") private String asuntoCorreo = ASUNTO_PREDETERMINADO;
    @Column(name="mensaje_correo", nullable=false, columnDefinition="TEXT") private String mensajeCorreo = CORREO_PREDETERMINADO;
    @Column(name="whatsapp_habilitado", nullable=false) private boolean whatsappHabilitado;
    @Column(name="mensaje_whatsapp", nullable=false, columnDefinition="TEXT") private String mensajeWhatsapp = WHATSAPP_PREDETERMINADO;
    @Column(name="whatsapp_template_id") private String whatsappTemplateId;
    @Column(name="creado_en", nullable=false, updatable=false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name="actualizado_en", nullable=false) private LocalDateTime actualizadoEn = LocalDateTime.now();
    @Column(name="actualizado_por") private UUID actualizadoPor;

    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;}
    public String getEvento(){return evento;} public void setEvento(String v){evento=v;}
    public boolean isCorreoHabilitado(){return correoHabilitado;} public void setCorreoHabilitado(boolean v){correoHabilitado=v;}
    public String getSmtpFrom(){return smtpFrom;} public void setSmtpFrom(String v){smtpFrom=v;}
    public String getSmtpUsername(){return smtpUsername;} public void setSmtpUsername(String v){smtpUsername=v;}
    public String getAsuntoCorreo(){return asuntoCorreo;} public void setAsuntoCorreo(String v){asuntoCorreo=v;}
    public String getMensajeCorreo(){return mensajeCorreo;} public void setMensajeCorreo(String v){mensajeCorreo=v;}
    public boolean isWhatsappHabilitado(){return whatsappHabilitado;} public void setWhatsappHabilitado(boolean v){whatsappHabilitado=v;}
    public String getMensajeWhatsapp(){return mensajeWhatsapp;} public void setMensajeWhatsapp(String v){mensajeWhatsapp=v;}
    public String getWhatsappTemplateId(){return whatsappTemplateId;} public void setWhatsappTemplateId(String v){whatsappTemplateId=v;}
    public LocalDateTime getActualizadoEn(){return actualizadoEn;} public void setActualizadoEn(LocalDateTime v){actualizadoEn=v;}
    public UUID getActualizadoPor(){return actualizadoPor;} public void setActualizadoPor(UUID v){actualizadoPor=v;}
}
