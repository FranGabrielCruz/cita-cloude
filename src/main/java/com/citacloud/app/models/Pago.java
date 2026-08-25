package com.citacloud.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="pagos")
public class Pago {
 @Id @GeneratedValue private UUID id; @Column(name="empresa_id",nullable=false) private UUID empresaId;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="factura_id") private Factura factura;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="paciente_id") private Paciente paciente;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="sucursal_id") private Sucursal sucursal;
 @Column(name="numero") private String numero; @Column(name="usuario_id") private UUID usuarioId; private LocalDate fecha;
 @Column(name="creado_en",updatable=false) private LocalDateTime creadoEn=LocalDateTime.now(); private BigDecimal monto;
 @Column(name="metodo_pago") private String metodoPago; private String referencia; private String observacion; @Column(name="moneda") private String moneda="DOP";
 @Column(name="efectivo_recibido") private BigDecimal efectivoRecibido; @Column(name="cambio") private BigDecimal cambio=BigDecimal.ZERO; @Column(name="autorizacion") private String autorizacion; @Column(name="clave_idempotencia") private String claveIdempotencia;
 @Column(name="anulado_en") private LocalDateTime anuladoEn; @Column(name="anulado_por") private UUID anuladoPor; @Column(name="motivo_anulacion") private String motivoAnulacion; private String estado="APPLIED";
 public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;} public Factura getFactura(){return factura;} public void setFactura(Factura v){factura=v;} public Paciente getPaciente(){return paciente;} public void setPaciente(Paciente v){paciente=v;} public Sucursal getSucursal(){return sucursal;} public void setSucursal(Sucursal v){sucursal=v;} public String getNumero(){return numero;} public void setNumero(String v){numero=v;} public UUID getUsuarioId(){return usuarioId;} public void setUsuarioId(UUID v){usuarioId=v;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public LocalDateTime getCreadoEn(){return creadoEn;} public void setCreadoEn(LocalDateTime v){creadoEn=v;} public BigDecimal getMonto(){return monto;} public void setMonto(BigDecimal v){monto=v;} public String getMetodoPago(){return metodoPago;} public void setMetodoPago(String v){metodoPago=v;} public String getReferencia(){return referencia;} public void setReferencia(String v){referencia=v;} public String getObservacion(){return observacion;} public void setObservacion(String v){observacion=v;} public String getMoneda(){return moneda;} public void setMoneda(String v){moneda=v;} public BigDecimal getEfectivoRecibido(){return efectivoRecibido;} public void setEfectivoRecibido(BigDecimal v){efectivoRecibido=v;} public BigDecimal getCambio(){return cambio;} public void setCambio(BigDecimal v){cambio=v;} public String getAutorizacion(){return autorizacion;} public void setAutorizacion(String v){autorizacion=v;} public String getClaveIdempotencia(){return claveIdempotencia;} public void setClaveIdempotencia(String v){claveIdempotencia=v;} public void anular(UUID usuario,String motivo){anuladoEn=LocalDateTime.now();anuladoPor=usuario;motivoAnulacion=motivo;estado="VOIDED";} public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
}
