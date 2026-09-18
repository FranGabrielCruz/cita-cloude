package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_factura")
public class HistorialFactura {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false, updatable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factura_id", nullable = false, updatable = false) private Factura factura;
    @Column(name = "estado_anterior", length = 20, updatable = false) private String estadoAnterior;
    @Column(name = "estado_nuevo", nullable = false, length = 20, updatable = false) private String estadoNuevo;
    @Column(nullable = false, length = 40, updatable = false) private String accion;
    @Column(updatable = false, columnDefinition = "TEXT") private String motivo;
    @Column(name = "usuario_id", updatable = false) private UUID usuarioId;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;}
    public Factura getFactura(){return factura;} public void setFactura(Factura v){factura=v;} public String getEstadoAnterior(){return estadoAnterior;}
    public void setEstadoAnterior(String v){estadoAnterior=v;} public String getEstadoNuevo(){return estadoNuevo;} public void setEstadoNuevo(String v){estadoNuevo=v;}
    public String getAccion(){return accion;} public void setAccion(String v){accion=v;} public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;}
    public UUID getUsuarioId(){return usuarioId;} public void setUsuarioId(UUID v){usuarioId=v;} public LocalDateTime getCreadoEn(){return creadoEn;}
}
