package com.citacloud.app.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resultados_laboratorio")
public class ResultadoLaboratorio {
    @Id @GeneratedValue private UUID id;
    @Column(name="empresa_id", nullable=false) private UUID empresaId;
    @Column(name="orden_id", nullable=false) private UUID ordenId;
    @Column(name="detalle_orden_id", nullable=false) private UUID detalleOrdenId;
    @Column(name="muestra_id") private UUID muestraId;
    @Column(nullable=false) private String estado="BORRADOR";
    @Column(columnDefinition="TEXT") private String resultado;
    @Column(name="resultado_numerico") private BigDecimal resultadoNumerico;
    private String unidad;
    @Column(name="referencia_minima") private BigDecimal referenciaMinima;
    @Column(name="referencia_maxima") private BigDecimal referenciaMaxima;
    private String indicador;
    @Column(columnDefinition="TEXT") private String observacion;
    @Column(name="registrado_por") private UUID registradoPor;
    @Column(name="registrado_en") private LocalDateTime registradoEn;
    @Column(name="validado_por") private UUID validadoPor;
    @Column(name="validado_en") private LocalDateTime validadoEn;
    @Column(name="motivo_correccion", columnDefinition="TEXT") private String motivoCorreccion;
    private int version=1;
    @Column(name="creado_en") private LocalDateTime creadoEn=LocalDateTime.now();
    @Column(name="actualizado_en") private LocalDateTime actualizadoEn=LocalDateTime.now();
    @Version private long bloqueo;
    public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public UUID getOrdenId(){return ordenId;} public UUID getDetalleOrdenId(){return detalleOrdenId;} public UUID getMuestraId(){return muestraId;} public String getEstado(){return estado;} public String getResultado(){return resultado;} public BigDecimal getResultadoNumerico(){return resultadoNumerico;} public String getUnidad(){return unidad;} public BigDecimal getReferenciaMinima(){return referenciaMinima;} public BigDecimal getReferenciaMaxima(){return referenciaMaxima;} public String getIndicador(){return indicador;} public String getObservacion(){return observacion;} public UUID getRegistradoPor(){return registradoPor;} public LocalDateTime getRegistradoEn(){return registradoEn;} public UUID getValidadoPor(){return validadoPor;} public LocalDateTime getValidadoEn(){return validadoEn;} public int getVersion(){return version;}
    public void setEmpresaId(UUID v){empresaId=v;} public void setOrdenId(UUID v){ordenId=v;} public void setDetalleOrdenId(UUID v){detalleOrdenId=v;} public void setMuestraId(UUID v){muestraId=v;} public void setEstado(String v){estado=v;} public void setResultado(String v){resultado=v;} public void setResultadoNumerico(BigDecimal v){resultadoNumerico=v;} public void setUnidad(String v){unidad=v;} public void setReferenciaMinima(BigDecimal v){referenciaMinima=v;} public void setReferenciaMaxima(BigDecimal v){referenciaMaxima=v;} public void setIndicador(String v){indicador=v;} public void setObservacion(String v){observacion=v;} public void setRegistradoPor(UUID v){registradoPor=v;} public void setRegistradoEn(LocalDateTime v){registradoEn=v;} public void setValidadoPor(UUID v){validadoPor=v;} public void setValidadoEn(LocalDateTime v){validadoEn=v;} public void setMotivoCorreccion(String v){motivoCorreccion=v;} public void setVersion(int v){version=v;} public void setActualizadoEn(LocalDateTime v){actualizadoEn=v;}
}
