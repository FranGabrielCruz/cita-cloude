package com.citacloud.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cargos_financieros")
public class CargoFinanciero {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "paciente_id", nullable = false) private Paciente paciente;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "sucursal_id") private Sucursal sucursal;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factura_id") private Factura factura;
    @Column(nullable = false) private String origen;
    @Column(name = "referencia_origen") private String referenciaOrigen;
    @Column(nullable = false) private String concepto;
    @Column(nullable = false) private LocalDate fecha;
    @Column(name = "monto_original", nullable = false) private BigDecimal montoOriginal;
    @Column(name = "monto_pagado", nullable = false) private BigDecimal montoPagado = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal saldo;
    @Column(nullable = false) private String estado = "PENDIENTE";
    @Version private Long version;
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;}
    public Paciente getPaciente(){return paciente;} public void setPaciente(Paciente v){paciente=v;} public Sucursal getSucursal(){return sucursal;} public void setSucursal(Sucursal v){sucursal=v;}
    public Factura getFactura(){return factura;} public void setFactura(Factura v){factura=v;} public String getOrigen(){return origen;} public void setOrigen(String v){origen=v;}
    public String getReferenciaOrigen(){return referenciaOrigen;} public void setReferenciaOrigen(String v){referenciaOrigen=v;} public String getConcepto(){return concepto;} public void setConcepto(String v){concepto=v;}
    public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public BigDecimal getMontoOriginal(){return montoOriginal;} public void setMontoOriginal(BigDecimal v){montoOriginal=v;}
    public BigDecimal getMontoPagado(){return montoPagado;} public void setMontoPagado(BigDecimal v){montoPagado=v;} public BigDecimal getSaldo(){return saldo;} public void setSaldo(BigDecimal v){saldo=v;}
    public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
}
