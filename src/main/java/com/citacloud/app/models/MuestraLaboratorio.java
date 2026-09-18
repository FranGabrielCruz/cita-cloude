package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "muestras_laboratorio")
public class MuestraLaboratorio {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @Column(name = "orden_id", nullable = false) private UUID ordenId;
    @Column(name = "sucursal_id") private UUID sucursalId;
    @Column(nullable = false) private String codigo;
    @Column(name = "tipo_muestra", nullable = false) private String tipoMuestra;
    @Column(nullable = false) private String estado = "RECIBIDA";
    @Column(name = "tomada_en", nullable = false) private LocalDateTime tomadaEn = LocalDateTime.now();
    @Column(name = "recibida_por") private UUID recibidaPor;
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT") private String motivoRechazo;
    @Column(columnDefinition = "TEXT") private String observacion;
    @Column(name = "creado_en") private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name = "actualizado_en") private LocalDateTime actualizadoEn = LocalDateTime.now();
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public UUID getOrdenId(){return ordenId;} public UUID getSucursalId(){return sucursalId;} public String getCodigo(){return codigo;} public String getTipoMuestra(){return tipoMuestra;} public String getEstado(){return estado;} public LocalDateTime getTomadaEn(){return tomadaEn;} public UUID getRecibidaPor(){return recibidaPor;} public String getMotivoRechazo(){return motivoRechazo;} public String getObservacion(){return observacion;}
    public void setEmpresaId(UUID v){empresaId=v;} public void setOrdenId(UUID v){ordenId=v;} public void setSucursalId(UUID v){sucursalId=v;} public void setCodigo(String v){codigo=v;} public void setTipoMuestra(String v){tipoMuestra=v;} public void setEstado(String v){estado=v;} public void setTomadaEn(LocalDateTime v){tomadaEn=v;} public void setRecibidaPor(UUID v){recibidaPor=v;} public void setMotivoRechazo(String v){motivoRechazo=v;} public void setObservacion(String v){observacion=v;} public void setActualizadoEn(LocalDateTime v){actualizadoEn=v;}
}
