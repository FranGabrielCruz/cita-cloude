package com.citacloud.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Linea comercial inmutable una vez emitida la factura. */
@Entity
@Table(name = "detalle_factura")
public class DetalleFactura {
    @Id @GeneratedValue private UUID id;
    @Column(name = "empresa_id", nullable = false, updatable = false) private UUID empresaId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factura_id", nullable = false, updatable = false) private Factura factura;
    @Column(name = "tipo_item", nullable = false, length = 20) private String tipoItem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "servicio_id") private Servicio servicio;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "producto_id") private ProductoInventario producto;
    @Column(name = "codigo_snapshot", nullable = false, length = 80) private String codigoSnapshot;
    @Column(name = "descripcion", nullable = false, length = 255) private String descripcion;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal cantidad;
    @Column(name = "precio", nullable = false, precision = 14, scale = 2) private BigDecimal precio;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal descuento;
    @Column(name = "tasa_impuesto_snapshot", nullable = false, precision = 7, scale = 4) private BigDecimal tasaImpuesto;
    @Column(name = "impuesto", nullable = false, precision = 14, scale = 2) private BigDecimal impuesto;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal subtotal;
    @Column(name = "importe", nullable = false, precision = 14, scale = 2) private BigDecimal importe;
    @Column(name = "creado_en", nullable = false, updatable = false) private LocalDateTime creadoEn = LocalDateTime.now();

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public ProductoInventario getProducto() { return producto; }
    public void setProducto(ProductoInventario producto) { this.producto = producto; }
    public String getCodigoSnapshot() { return codigoSnapshot; }
    public void setCodigoSnapshot(String codigoSnapshot) { this.codigoSnapshot = codigoSnapshot; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getPrecioUnitario() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getTasaImpuesto() { return tasaImpuesto; }
    public void setTasaImpuesto(BigDecimal tasaImpuesto) { this.tasaImpuesto = tasaImpuesto; }
    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getImporte() { return importe; }
    public BigDecimal getTotal() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
