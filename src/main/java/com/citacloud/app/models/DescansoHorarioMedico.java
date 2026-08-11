package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "descansos_horarios_medicos")
public class DescansoHorarioMedico {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @Column(name = "horario_id", nullable = false) private UUID horarioId;
    @Column(name = "hora_inicio", nullable = false) private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false) private LocalTime horaFin;
    @Column(length = 200) private String descripcion;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; } public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public UUID getHorarioId() { return horarioId; } public void setHorarioId(UUID horarioId) { this.horarioId = horarioId; }
    public LocalTime getHoraInicio() { return horaInicio; } public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; } public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getCreadoEn() { return creadoEn; } public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
