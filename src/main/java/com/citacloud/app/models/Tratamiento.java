package com.citacloud.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tratamientos")
public class Tratamiento {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @Column(name = "paciente_id", nullable = false) private UUID pacienteId;
    @Column(name = "diagnostico_id") private UUID diagnosticoId;
    private String tipo;
    @Column(nullable = false, columnDefinition = "TEXT") private String descripcion;
    @Column(name = "fecha_inicio") private LocalDate fechaInicio;
    @Column(name = "fecha_fin") private LocalDate fechaFin;
    @Column(nullable = false, columnDefinition = "TEXT") private String indicaciones;
    private String estado = "ACTIVO";
    @Column(columnDefinition = "TEXT") private String observaciones;
    @Column(name = "registrado_por") private UUID registradoPor;
    @Column(name = "creado_en") private LocalDateTime creadoEn;
    @Column(name = "actualizado_en") private LocalDateTime actualizadoEn;

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getDiagnosticoId() { return diagnosticoId; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getIndicaciones() { return indicaciones; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public UUID getRegistradoPor() { return registradoPor; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setEmpresaId(UUID value) { empresaId = value; }
    public void setPacienteId(UUID value) { pacienteId = value; }
    public void setDiagnosticoId(UUID value) { diagnosticoId = value; }
    public void setTipo(String value) { tipo = value; }
    public void setDescripcion(String value) { descripcion = value; }
    public void setFechaInicio(LocalDate value) { fechaInicio = value; }
    public void setFechaFin(LocalDate value) { fechaFin = value; }
    public void setIndicaciones(String value) { indicaciones = value; }
    public void setEstado(String value) { estado = value; }
    public void setObservaciones(String value) { observaciones = value; }
    public void setRegistradoPor(UUID value) { registradoPor = value; }
    public void setCreadoEn(LocalDateTime value) { creadoEn = value; }
    public void setActualizadoEn(LocalDateTime value) { actualizadoEn = value; }
}
