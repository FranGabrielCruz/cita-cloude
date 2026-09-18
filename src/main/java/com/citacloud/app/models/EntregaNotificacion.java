package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name="entregas_notificacion", uniqueConstraints=@UniqueConstraint(columnNames={"empresa_id","entidad_id","evento","canal"}))
public class EntregaNotificacion {
    @Id @GeneratedValue private UUID id;
    @Column(name="empresa_id",nullable=false) private UUID empresaId;
    @Column(nullable=false) private String evento;
    @Column(name="entidad_tipo",nullable=false) private String entidadTipo;
    @Column(name="entidad_id",nullable=false) private UUID entidadId;
    @Column(nullable=false) private String canal;
    private String destinatario;
    @Column(name="destinatario_enmascarado") private String destinatarioEnmascarado;
    @Column(columnDefinition="TEXT") private String asunto;
    @Column(name="mensaje_renderizado",columnDefinition="TEXT") private String mensajeRenderizado;
    @Column(nullable=false) private String estado;
    private String proveedor;
    @Column(name="referencia_externa") private String referenciaExterna;
    @Column(name="cantidad_intentos",nullable=false) private int cantidadIntentos;
    @Column(name="codigo_fallo") private String codigoFallo;
    @Column(name="motivo_fallo") private String motivoFallo;
    @Column(name="creada_en",nullable=false,updatable=false) private LocalDateTime creadaEn=LocalDateTime.now();
    @Column(name="enviada_en") private LocalDateTime enviadaEn;
    @Column(name="actualizada_en",nullable=false) private LocalDateTime actualizadaEn=LocalDateTime.now();
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;}
    public String getEvento(){return evento;} public void setEvento(String v){evento=v;} public String getEntidadTipo(){return entidadTipo;} public void setEntidadTipo(String v){entidadTipo=v;} public UUID getEntidadId(){return entidadId;} public void setEntidadId(UUID v){entidadId=v;}
    public String getCanal(){return canal;} public void setCanal(String v){canal=v;} public String getDestinatario(){return destinatario;} public void setDestinatario(String v){destinatario=v;} public String getDestinatarioEnmascarado(){return destinatarioEnmascarado;} public void setDestinatarioEnmascarado(String v){destinatarioEnmascarado=v;}
    public String getAsunto(){return asunto;} public void setAsunto(String v){asunto=v;} public String getMensajeRenderizado(){return mensajeRenderizado;} public void setMensajeRenderizado(String v){mensajeRenderizado=v;} public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
    public String getProveedor(){return proveedor;} public void setProveedor(String v){proveedor=v;} public String getReferenciaExterna(){return referenciaExterna;} public void setReferenciaExterna(String v){referenciaExterna=v;} public int getCantidadIntentos(){return cantidadIntentos;} public void setCantidadIntentos(int v){cantidadIntentos=v;}
    public String getCodigoFallo(){return codigoFallo;} public void setCodigoFallo(String v){codigoFallo=v;} public String getMotivoFallo(){return motivoFallo;} public void setMotivoFallo(String v){motivoFallo=v;} public LocalDateTime getCreadaEn(){return creadaEn;} public LocalDateTime getEnviadaEn(){return enviadaEn;} public void setEnviadaEn(LocalDateTime v){enviadaEn=v;} public void setActualizadaEn(LocalDateTime v){actualizadaEn=v;}
}
