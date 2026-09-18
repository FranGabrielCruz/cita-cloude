package com.citacloud.app.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "secuencias_comprobante_fiscal")
public class SecuenciaComprobanteFiscal {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false, updatable = false) private UUID empresaId;
    @Column(nullable = false, length = 30) private String tipo;
    @Column(nullable = false, length = 120) private String nombre;
    @Column(nullable = false, length = 20) private String prefijo;
    @Column(name = "numero_desde", nullable = false) private Long numeroDesde;
    @Column(name = "numero_hasta", nullable = false) private Long numeroHasta;
    @Column(name = "numero_siguiente", nullable = false) private Long numeroSiguiente;
    @Column(nullable = false) private Boolean activa = true;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name = "actualizado_en", nullable = false) private LocalDateTime actualizadoEn = LocalDateTime.now();
    @Version private Long version;

    @PreUpdate void actualizarMarcaTemporal() { actualizadoEn = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }
    public Long getNumeroDesde() { return numeroDesde; }
    public void setNumeroDesde(Long numeroDesde) { this.numeroDesde = numeroDesde; }
    public Long getNumeroHasta() { return numeroHasta; }
    public void setNumeroHasta(Long numeroHasta) { this.numeroHasta = numeroHasta; }
    public Long getNumeroSiguiente() { return numeroSiguiente; }
    public void setNumeroSiguiente(Long numeroSiguiente) { this.numeroSiguiente = numeroSiguiente; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public Long getVersion() { return version; }
}
