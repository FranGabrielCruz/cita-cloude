package com.citacloud.app.models;
import jakarta.persistence.*; import java.math.BigDecimal; import java.util.UUID;
@Entity @Table(name="pago_aplicaciones") public class PagoAplicacion {
 @Id @GeneratedValue private UUID id; @Column(name="empresa_id",nullable=false) private UUID empresaId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pago_id",nullable=false) private Pago pago;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="cargo_id",nullable=false) private CargoFinanciero cargo;
 @Column(nullable=false) private BigDecimal monto;
 public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;} public Pago getPago(){return pago;} public void setPago(Pago v){pago=v;} public CargoFinanciero getCargo(){return cargo;} public void setCargo(CargoFinanciero v){cargo=v;} public BigDecimal getMonto(){return monto;} public void setMonto(BigDecimal v){monto=v;}
}
