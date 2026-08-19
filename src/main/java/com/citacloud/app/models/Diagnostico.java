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
@Table(name = "diagnosticos")
public class Diagnostico {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @Column(name = "paciente_id", nullable = false) private UUID pacienteId;
    private String codigo;
    @Column(nullable = false, columnDefinition = "TEXT") private String descripcion;
    private String tipo;
    private boolean principal;
    private String estado = "ACTIVO";
    @Column(name = "fecha_diagnostico") private LocalDate fechaDiagnostico;
    @Column(columnDefinition = "TEXT") private String observaciones;
    @Column(name = "registrado_por") private UUID registradoPor;
    @Column(name = "creado_en") private LocalDateTime creadoEn;
    @Column(name = "actualizado_en") private LocalDateTime actualizadoEn;

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getPacienteId() { return pacienteId; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getTipo() { return tipo; }
    public boolean isPrincipal() { return principal; }
    public String getEstado() { return estado; }
    public LocalDate getFechaDiagnostico() { return fechaDiagnostico; }
    public String getObservaciones() { return observaciones; }
    public UUID getRegistradoPor() { return registradoPor; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setEmpresaId(UUID value) { empresaId = value; }
    public void setPacienteId(UUID value) { pacienteId = value; }
    public void setCodigo(String value) { codigo = value; }
    public void setDescripcion(String value) { descripcion = value; }
    public void setTipo(String value) { tipo = value; }
    public void setPrincipal(boolean value) { principal = value; }
    public void setEstado(String value) { estado = value; }
    public void setFechaDiagnostico(LocalDate value) { fechaDiagnostico = value; }
    public void setObservaciones(String value) { observaciones = value; }
    public void setRegistradoPor(UUID value) { registradoPor = value; }
    public void setCreadoEn(LocalDateTime value) { creadoEn = value; }
    public void setActualizadoEn(LocalDateTime value) { actualizadoEn = value; }
}
