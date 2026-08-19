package com.citacloud.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name="signos_vitales")
public class SignosVitales {
    @Id @GeneratedValue private UUID id;
    @Column(name="empresa_id") private UUID empresaId;
    @Column(name="paciente_id") private UUID pacienteId;
    @Column(name="cita_id") private UUID citaId;
    private BigDecimal peso, altura, temperatura, imc, saturacionOxigeno, glucemia, circunferenciaAbdominal;
    @Column(name="presion_sistolica") private Integer presionSistolica;
    @Column(name="presion_diastolica") private Integer presionDiastolica;
    @Column(name="frecuencia_cardiaca") private Integer frecuenciaCardiaca;
    @Column(name="frecuencia_respiratoria") private Integer frecuenciaRespiratoria;
    @Column(name="nivel_dolor") private Integer nivelDolor;
    @Column(columnDefinition="TEXT") private String observaciones;
    @Column(name="usuario_id") private UUID usuarioId;
    @Column(name="fecha", nullable=false) private LocalDateTime fecha;
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public UUID getPacienteId(){return pacienteId;}
    public BigDecimal getPeso(){return peso;} public BigDecimal getAltura(){return altura;} public BigDecimal getTemperatura(){return temperatura;} public BigDecimal getImc(){return imc;} public BigDecimal getSaturacionOxigeno(){return saturacionOxigeno;} public BigDecimal getGlucemia(){return glucemia;} public BigDecimal getCircunferenciaAbdominal(){return circunferenciaAbdominal;}
    public Integer getPresionSistolica(){return presionSistolica;} public Integer getPresionDiastolica(){return presionDiastolica;} public Integer getFrecuenciaCardiaca(){return frecuenciaCardiaca;} public Integer getFrecuenciaRespiratoria(){return frecuenciaRespiratoria;} public Integer getNivelDolor(){return nivelDolor;} public String getObservaciones(){return observaciones;} public UUID getUsuarioId(){return usuarioId;} public LocalDateTime getFecha(){return fecha;}
    public void setEmpresaId(UUID v){empresaId=v;} public void setPacienteId(UUID v){pacienteId=v;} public void setPeso(BigDecimal v){peso=v;} public void setAltura(BigDecimal v){altura=v;} public void setTemperatura(BigDecimal v){temperatura=v;} public void setImc(BigDecimal v){imc=v;} public void setPresionSistolica(Integer v){presionSistolica=v;} public void setPresionDiastolica(Integer v){presionDiastolica=v;} public void setFrecuenciaCardiaca(Integer v){frecuenciaCardiaca=v;} public void setFrecuenciaRespiratoria(Integer v){frecuenciaRespiratoria=v;} public void setSaturacionOxigeno(BigDecimal v){saturacionOxigeno=v;} public void setGlucemia(BigDecimal v){glucemia=v;} public void setCircunferenciaAbdominal(BigDecimal v){circunferenciaAbdominal=v;} public void setNivelDolor(Integer v){nivelDolor=v;} public void setObservaciones(String v){observaciones=v;} public void setUsuarioId(UUID v){usuarioId=v;} public void setFecha(LocalDateTime v){fecha=v;}
}
