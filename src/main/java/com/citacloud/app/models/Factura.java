package com.citacloud.app.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "facturas")
public class Factura {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false, updatable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "sucursal_id") private Sucursal sucursal;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "paciente_id", nullable = false) private Paciente paciente;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "medico_id") private Medico medico;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "caja_id") private Caja caja;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "sesion_caja_id") private SesionCaja sesionCaja;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "secuencia_comprobante_id") private SecuenciaComprobanteFiscal secuenciaComprobante;
    @Column(nullable = false, length = 40) private String numero;
    @Column(nullable = false) private LocalDate fecha;
    @Column(name = "tipo_comprobante", length = 30) private String tipoComprobante;
    @Column(name = "numero_comprobante_fiscal", length = 40) private String numeroComprobanteFiscal;
    @Column(nullable = false, length = 3) private String moneda = "DOP";
    @Column(name = "origen_tipo", length = 40) private String origenTipo;
    @Column(name = "origen_id") private UUID origenId;
    @Column(columnDefinition = "TEXT") private String observacion;
    @Column(name = "clave_idempotencia", length = 100) private String claveIdempotencia;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal descuento = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal impuestos = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @Column(name = "monto_pagado", nullable = false, precision = 14, scale = 2) private BigDecimal montoPagado = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal saldo = BigDecimal.ZERO;
    @Column(nullable = false, length = 20) private String estado = "BORRADOR";
    @Column(name = "estado_ecf", nullable = false, length = 30) private String estadoEcf = "NO_APLICA";
    @Column(name = "creado_por", updatable = false) private UUID creadoPor;
    @Column(name = "creado_en", updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name = "actualizado_en", nullable = false) private LocalDateTime actualizadoEn = LocalDateTime.now();
    @Column(name = "emitido_por") private UUID emitidoPor;
    @Column(name = "emitido_en") private LocalDateTime emitidoEn;
    @Column(name = "anulado_por") private UUID anuladoPor;
    @Column(name = "anulado_en") private LocalDateTime anuladoEn;
    @Column(name = "motivo_anulacion", length = 120) private String motivoAnulacion;
    @Column(name = "observacion_anulacion", columnDefinition = "TEXT") private String observacionAnulacion;
    @Version private Long version;

    @PreUpdate void actualizarMarcaTemporal() { actualizadoEn = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public Caja getCaja() { return caja; }
    public void setCaja(Caja caja) { this.caja = caja; }
    public SesionCaja getSesionCaja() { return sesionCaja; }
    public void setSesionCaja(SesionCaja sesionCaja) { this.sesionCaja = sesionCaja; }
    public SecuenciaComprobanteFiscal getSecuenciaComprobante() { return secuenciaComprobante; }
    public void setSecuenciaComprobante(SecuenciaComprobanteFiscal secuenciaComprobante) { this.secuenciaComprobante = secuenciaComprobante; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getNumeroComprobanteFiscal() { return numeroComprobanteFiscal; }
    public void setNumeroComprobanteFiscal(String numeroComprobanteFiscal) { this.numeroComprobanteFiscal = numeroComprobanteFiscal; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getOrigenTipo() { return origenTipo; }
    public void setOrigenTipo(String origenTipo) { this.origenTipo = origenTipo; }
    public UUID getOrigenId() { return origenId; }
    public void setOrigenId(UUID origenId) { this.origenId = origenId; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(String claveIdempotencia) { this.claveIdempotencia = claveIdempotencia; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getEstadoEcf() { return estadoEcf; }
    public void setEstadoEcf(String estadoEcf) { this.estadoEcf = estadoEcf; }
    public UUID getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UUID creadoPor) { this.creadoPor = creadoPor; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public UUID getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(UUID emitidoPor) { this.emitidoPor = emitidoPor; }
    public LocalDateTime getEmitidoEn() { return emitidoEn; }
    public void setEmitidoEn(LocalDateTime emitidoEn) { this.emitidoEn = emitidoEn; }
    public UUID getAnuladoPor() { return anuladoPor; }
    public void setAnuladoPor(UUID anuladoPor) { this.anuladoPor = anuladoPor; }
    public LocalDateTime getAnuladoEn() { return anuladoEn; }
    public void setAnuladoEn(LocalDateTime anuladoEn) { this.anuladoEn = anuladoEn; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }
    public String getObservacionAnulacion() { return observacionAnulacion; }
    public void setObservacionAnulacion(String observacionAnulacion) { this.observacionAnulacion = observacionAnulacion; }
    public Long getVersion() { return version; }
}
