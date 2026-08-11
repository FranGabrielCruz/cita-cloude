package com.citacloud.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seguros_paciente")
public class SeguroPaciente {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "aseguradora_id", nullable = false)
    private UUID aseguradoraId;

    @Column(name = "numero_poliza", nullable = false)
    private String numeroPoliza;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public UUID getPacienteId() { return pacienteId; }
    public void setPacienteId(UUID pacienteId) { this.pacienteId = pacienteId; }
    public UUID getAseguradoraId() { return aseguradoraId; }
    public void setAseguradoraId(UUID aseguradoraId) { this.aseguradoraId = aseguradoraId; }
    public String getNumeroPoliza() { return numeroPoliza; }
    public void setNumeroPoliza(String numeroPoliza) { this.numeroPoliza = numeroPoliza; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
