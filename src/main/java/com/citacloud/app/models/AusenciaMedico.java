package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ausencias_medicos")
public class AusenciaMedico {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "medico_id", nullable = false) private Medico medico;
    @Column(name = "fecha_inicio", nullable = false) private LocalDateTime fechaInicio;
    @Column(name = "fecha_fin", nullable = false) private LocalDateTime fechaFin;
    @Column(columnDefinition = "TEXT") private String motivo;
    @Column(nullable = false) private Boolean activo = true;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; } public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Medico getMedico() { return medico; } public void setMedico(Medico medico) { this.medico = medico; }
    public LocalDateTime getFechaInicio() { return fechaInicio; } public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; } public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public String getMotivo() { return motivo; } public void setMotivo(String motivo) { this.motivo = motivo; }
    public Boolean getActivo() { return activo; } public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; } public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
