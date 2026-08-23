package com.citacloud.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos")
public class Documento {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id") private UUID empresaId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "paciente_id") private Paciente paciente;
    private String nombre;
    private String tipo;
    private String descripcion;
    @Column(name = "ruta_archivo") private String rutaArchivo;
    @Column(name = "nombre_archivo") private String nombreArchivo;
    @Column(name = "usuario_id") private UUID usuarioId;
    private LocalDateTime fecha = LocalDateTime.now();
    private String estado = "ACTIVO";
    private String origen = "MANUAL";
    @Column(name = "archivado_en") private LocalDateTime archivadoEn;
    @Column(name = "archivado_por") private UUID archivadoPor;
    @Column(name = "motivo_archivo") private String motivoArchivo;
    @Column(name = "actualizado_en") private LocalDateTime actualizadoEn = LocalDateTime.now();

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID valor) { empresaId = valor; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente valor) { paciente = valor; }
    public String getNombre() { return nombre; }
    public void setNombre(String valor) { nombre = valor; }
    public String getTipo() { return tipo; }
    public void setTipo(String valor) { tipo = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String valor) { descripcion = valor; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String valor) { rutaArchivo = valor; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String valor) { nombreArchivo = valor; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID valor) { usuarioId = valor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime valor) { fecha = valor; }
    public String getEstado() { return estado; }
    public void setEstado(String valor) { estado = valor; }
    public String getOrigen() { return origen; }
    public void setOrigen(String valor) { origen = valor; }
    public LocalDateTime getArchivadoEn() { return archivadoEn; }
    public void setArchivadoEn(LocalDateTime valor) { archivadoEn = valor; }
    public UUID getArchivadoPor() { return archivadoPor; }
    public void setArchivadoPor(UUID valor) { archivadoPor = valor; }
    public String getMotivoArchivo() { return motivoArchivo; }
    public void setMotivoArchivo(String valor) { motivoArchivo = valor; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime valor) { actualizadoEn = valor; }
}
