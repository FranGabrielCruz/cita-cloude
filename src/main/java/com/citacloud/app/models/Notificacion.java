package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    private String tipo, categoria, prioridad, titulo;
    @Column(columnDefinition = "TEXT") private String mensaje;
    @Column(name = "entidad_tipo") private String entidadTipo;
    @Column(name = "entidad_id") private UUID entidadId;
    @Column(name = "creada_en") private LocalDateTime creadaEn = LocalDateTime.now();

    // El estado leído pertenece a cada destinatario, no a la notificación global.
    @Transient private boolean leida;

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public String getTipo() { return tipo; }
    public String getCategoria() { return categoria; }
    public String getPrioridad() { return prioridad; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public String getEntidadTipo() { return entidadTipo; }
    public UUID getEntidadId() { return entidadId; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public boolean isLeida() { return leida; }
    public void setEmpresaId(UUID valor) { empresaId = valor; }
    public void setTipo(String valor) { tipo = valor; }
    public void setCategoria(String valor) { categoria = valor; }
    public void setPrioridad(String valor) { prioridad = valor; }
    public void setTitulo(String valor) { titulo = valor; }
    public void setMensaje(String valor) { mensaje = valor; }
    public void setEntidadTipo(String valor) { entidadTipo = valor; }
    public void setEntidadId(UUID valor) { entidadId = valor; }
    public void setLeida(boolean valor) { leida = valor; }
}
