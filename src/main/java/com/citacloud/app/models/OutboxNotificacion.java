package com.citacloud.app.models;
import jakarta.persistence.*; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="outbox_notificaciones") public class OutboxNotificacion {
 @Id @GeneratedValue private UUID id; @Column(name="empresa_id",nullable=false) private UUID empresaId;
 @OneToOne(fetch=FetchType.EAGER) @JoinColumn(name="entrega_id",nullable=false,unique=true) private EntregaNotificacion entrega;
 @Column(nullable=false) private String estado="PENDIENTE"; @Column(nullable=false) private int intentos;
 @Column(name="proximo_intento",nullable=false) private LocalDateTime proximoIntento=LocalDateTime.now(); @Column(name="creado_en",nullable=false,updatable=false) private LocalDateTime creadoEn=LocalDateTime.now(); @Column(name="procesado_en") private LocalDateTime procesadoEn;
 public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;} public EntregaNotificacion getEntrega(){return entrega;} public void setEntrega(EntregaNotificacion v){entrega=v;} public String getEstado(){return estado;} public void setEstado(String v){estado=v;} public int getIntentos(){return intentos;} public void setIntentos(int v){intentos=v;} public LocalDateTime getProximoIntento(){return proximoIntento;} public void setProximoIntento(LocalDateTime v){proximoIntento=v;} public LocalDateTime getCreadoEn(){return creadoEn;} public LocalDateTime getProcesadoEn(){return procesadoEn;} public void setProcesadoEn(LocalDateTime v){procesadoEn=v;}
}
