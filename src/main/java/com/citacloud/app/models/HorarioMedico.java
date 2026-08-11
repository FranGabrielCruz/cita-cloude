package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "horarios_medicos")
public class HorarioMedico {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "medico_id", nullable = false) private Medico medico;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "sucursal_id", nullable = false) private Sucursal sucursal;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "consultorio_id") private Consultorio consultorio;
    @Column(name = "dia_semana", nullable = false) private Integer diaSemana;
    @Column(name = "hora_inicio", nullable = false) private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false) private LocalTime horaFin;
    @Column(name = "duracion_cita_minutos", nullable = false) private Integer duracionCitaMinutos = 30;
    @Column(nullable = false) private Boolean activo = true;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; } public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Medico getMedico() { return medico; } public void setMedico(Medico medico) { this.medico = medico; }
    public Sucursal getSucursal() { return sucursal; } public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Consultorio getConsultorio() { return consultorio; } public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }
    public Integer getDiaSemana() { return diaSemana; } public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; } public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; } public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public Integer getDuracionCitaMinutos() { return duracionCitaMinutos; } public void setDuracionCitaMinutos(Integer valor) { this.duracionCitaMinutos = valor; }
    public Boolean getActivo() { return activo; } public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; } public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
