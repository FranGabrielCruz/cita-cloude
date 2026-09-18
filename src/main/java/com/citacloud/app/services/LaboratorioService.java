package com.citacloud.app.services;

import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LaboratorioService {
    public record Resumen(long pendientes, long muestras, long enProceso, long porValidar) {}
    public record RegistroResultado(UUID detalleOrdenId, UUID muestraId, String resultado, BigDecimal resultadoNumerico,
                                    String unidad, BigDecimal referenciaMinima, BigDecimal referenciaMaxima, String observacion) {}
    private final OrdenEstudioRepository ordenes;
    private final DetalleOrdenEstudioRepository detalles;
    private final MuestraLaboratorioRepository muestras;
    private final ResultadoLaboratorioRepository resultados;
    private final AuditoriaService auditoria;
    private final JdbcTemplate jdbc;

    public LaboratorioService(OrdenEstudioRepository ordenes, DetalleOrdenEstudioRepository detalles,
                              MuestraLaboratorioRepository muestras, ResultadoLaboratorioRepository resultados,
                              AuditoriaService auditoria, JdbcTemplate jdbc) {
        this.ordenes=ordenes; this.detalles=detalles; this.muestras=muestras; this.resultados=resultados; this.auditoria=auditoria; this.jdbc=jdbc;
    }

    @Transactional(readOnly = true)
    public List<OrdenEstudio> ordenes(UUID empresa, String texto, String estado) {
        String filtro=normalizar(texto), estadoFiltro=normalizar(estado);
        return ordenes.findAll().stream().filter(o -> empresa.equals(o.getEmpresaId()))
                .filter(o -> !"BORRADOR".equals(o.getEstado()))
                .filter(o -> estudiosLaboratorio(o.getId()).size() > 0)
                .filter(o -> estadoFiltro.isBlank() || estadoOrden(empresa,o).equals(estadoFiltro))
                .filter(o -> filtro.isBlank() || normalizar(o.getNumero()).contains(filtro) || detalles.findByOrdenId(o.getId()).stream().anyMatch(d -> normalizar(d.getEstudio()).contains(filtro)))
                .sorted(Comparator.comparing(OrdenEstudio::getEmitidoEn, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    @Transactional(readOnly = true)
    public Resumen resumen(UUID empresa) {
        List<OrdenEstudio> ordenesLaboratorio=ordenes(empresa,null,null);
        return new Resumen(ordenesLaboratorio.stream().filter(o -> "PENDIENTE".equals(estadoOrden(empresa,o))).count(),
                muestras.countByEmpresaIdAndEstado(empresa,"RECIBIDA"),
                ordenesLaboratorio.stream().filter(o -> "EN_PROCESO".equals(estadoOrden(empresa,o))).count(),
                resultados.countByEmpresaIdAndEstado(empresa,"POR_VALIDAR"));
    }

    @Transactional(readOnly = true)
    public OrdenEstudio orden(UUID empresa, UUID ordenId) { return buscarOrden(empresa,ordenId); }
    @Transactional(readOnly = true)
    public List<DetalleOrdenEstudio> estudios(UUID empresa, UUID ordenId) { buscarOrden(empresa,ordenId); return estudiosLaboratorio(ordenId); }
    @Transactional(readOnly = true)
    public List<MuestraLaboratorio> muestras(UUID empresa, UUID ordenId) { buscarOrden(empresa,ordenId); return muestras.findByEmpresaIdAndOrdenIdOrderByTomadaEnDesc(empresa,ordenId); }
    @Transactional(readOnly = true)
    public List<ResultadoLaboratorio> resultados(UUID empresa, UUID ordenId) { buscarOrden(empresa,ordenId); return resultados.findByEmpresaIdAndOrdenIdOrderByCreadoEnDesc(empresa,ordenId); }

    @Transactional
    public MuestraLaboratorio registrarMuestra(UUID empresa, UUID ordenId, String tipoMuestra, String observacion) {
        OrdenEstudio orden=buscarOrden(empresa,ordenId);
        if (tipoMuestra==null || tipoMuestra.isBlank()) throw new IllegalArgumentException("El tipo de muestra es obligatorio.");
        TenantUserDetails usuario=AuthService.getAuthenticatedUser();
        MuestraLaboratorio muestra=new MuestraLaboratorio();
        muestra.setEmpresaId(empresa); muestra.setOrdenId(orden.getId()); muestra.setTipoMuestra(tipoMuestra.trim());
        muestra.setCodigo(siguienteCodigoMuestra(empresa)); muestra.setEstado("RECIBIDA"); muestra.setTomadaEn(LocalDateTime.now());
        muestra.setRecibidaPor(usuario==null?null:usuario.getUsuarioId()); muestra.setObservacion(vacio(observacion));
        muestra=muestras.saveAndFlush(muestra);
        for (DetalleOrdenEstudio detalle:estudiosLaboratorio(ordenId)) { jdbc.update("INSERT INTO muestra_laboratorio_estudios(muestra_id, detalle_orden_id) VALUES (?, ?) ON CONFLICT DO NOTHING",muestra.getId(),detalle.getId()); if ("PENDIENTE".equals(detalle.getEstado())) { detalle.setEstado("MUESTRA_TOMADA"); detalle.setActualizadoEn(LocalDateTime.now()); detalles.save(detalle); } }
        auditar(empresa,"REGISTRAR_MUESTRA","MUESTRA_LABORATORIO",muestra.getId(),muestra.getCodigo());
        return muestra;
    }

    @Transactional
    public void rechazarMuestra(UUID empresa, UUID muestraId, String motivo) {
        if (motivo==null || motivo.isBlank()) throw new IllegalArgumentException("El motivo de rechazo es obligatorio.");
        MuestraLaboratorio muestra=muestras.findByIdAndEmpresaId(muestraId,empresa).orElseThrow(() -> new IllegalArgumentException("Muestra no encontrada."));
        if ("PROCESADA".equals(muestra.getEstado())) throw new IllegalArgumentException("No se puede rechazar una muestra procesada.");
        muestra.setEstado("RECHAZADA"); muestra.setMotivoRechazo(motivo.trim()); muestra.setActualizadoEn(LocalDateTime.now()); muestras.save(muestra);
        auditar(empresa,"RECHAZAR_MUESTRA","MUESTRA_LABORATORIO",muestra.getId(),motivo);
    }

    @Transactional
    public ResultadoLaboratorio guardarResultado(UUID empresa, RegistroResultado solicitud, boolean enviarValidacion) {
        if (solicitud==null || solicitud.detalleOrdenId()==null) throw new IllegalArgumentException("El estudio es obligatorio.");
        DetalleOrdenEstudio detalle=detalles.findById(solicitud.detalleOrdenId()).orElseThrow(() -> new IllegalArgumentException("Estudio no encontrado."));
        OrdenEstudio orden=buscarOrden(empresa,detalle.getOrdenId());
        if (solicitud.muestraId()!=null) {
            MuestraLaboratorio muestra=muestras.findByIdAndEmpresaId(solicitud.muestraId(),empresa).orElseThrow(() -> new IllegalArgumentException("Muestra no encontrada."));
            if (!muestra.getOrdenId().equals(orden.getId()) || "RECHAZADA".equals(muestra.getEstado())) throw new IllegalArgumentException("La muestra no puede utilizarse para este resultado.");
        }
        ResultadoLaboratorio resultado=resultados.findFirstByEmpresaIdAndDetalleOrdenIdOrderByVersionDesc(empresa,detalle.getId()).orElseGet(ResultadoLaboratorio::new);
        if ("VALIDADO".equals(resultado.getEstado())) throw new IllegalArgumentException("Un resultado validado requiere una corrección controlada.");
        if ((solicitud.resultado()==null || solicitud.resultado().isBlank()) && solicitud.resultadoNumerico()==null) throw new IllegalArgumentException("Debe registrar un resultado.");
        TenantUserDetails usuario=AuthService.getAuthenticatedUser();
        resultado.setEmpresaId(empresa); resultado.setOrdenId(orden.getId()); resultado.setDetalleOrdenId(detalle.getId()); resultado.setMuestraId(solicitud.muestraId());
        resultado.setResultado(vacio(solicitud.resultado())); resultado.setResultadoNumerico(solicitud.resultadoNumerico()); resultado.setUnidad(vacio(solicitud.unidad()));
        resultado.setReferenciaMinima(solicitud.referenciaMinima()); resultado.setReferenciaMaxima(solicitud.referenciaMaxima()); resultado.setIndicador(indicador(solicitud.resultadoNumerico(),solicitud.referenciaMinima(),solicitud.referenciaMaxima()));
        resultado.setObservacion(vacio(solicitud.observacion())); resultado.setRegistradoPor(usuario==null?null:usuario.getUsuarioId()); resultado.setRegistradoEn(LocalDateTime.now()); resultado.setActualizadoEn(LocalDateTime.now());
        resultado.setEstado(enviarValidacion?"POR_VALIDAR":"BORRADOR"); resultado=resultados.save(resultado);
        detalle.setEstado(enviarValidacion?"POR_VALIDAR":"EN_PROCESO"); detalle.setActualizadoEn(LocalDateTime.now()); detalles.save(detalle);
        auditar(empresa,enviarValidacion?"ENVIAR_VALIDACION":"GUARDAR_RESULTADO","RESULTADO_LABORATORIO",resultado.getId(),detalle.getEstudio());
        return resultado;
    }

    @Transactional
    public ResultadoLaboratorio validar(UUID empresa, UUID resultadoId) {
        ResultadoLaboratorio resultado=resultados.findByIdAndEmpresaId(resultadoId,empresa).orElseThrow(() -> new IllegalArgumentException("Resultado no encontrado."));
        if (!"POR_VALIDAR".equals(resultado.getEstado())) throw new IllegalArgumentException("Solo se pueden validar resultados enviados a validación.");
        TenantUserDetails usuario=AuthService.getAuthenticatedUser(); resultado.setEstado("VALIDADO"); resultado.setValidadoPor(usuario==null?null:usuario.getUsuarioId()); resultado.setValidadoEn(LocalDateTime.now()); resultado.setActualizadoEn(LocalDateTime.now()); resultado=resultados.save(resultado);
        DetalleOrdenEstudio detalle=detalles.findById(resultado.getDetalleOrdenId()).orElseThrow(); detalle.setEstado("VALIDADO"); detalle.setFechaResultado(LocalDateTime.now().toLocalDate()); detalle.setResultado(resultado.getResultado()!=null?resultado.getResultado():resultado.getResultadoNumerico().toPlainString()); detalle.setObservacionesResultado(resultado.getObservacion()); detalle.setRegistradoPor(resultado.getRegistradoPor()); detalle.setActualizadoEn(LocalDateTime.now()); detalles.save(detalle);
        auditar(empresa,"VALIDAR_RESULTADO","RESULTADO_LABORATORIO",resultado.getId(),detalle.getEstudio()); return resultado;
    }

    @Transactional
    public ResultadoLaboratorio completar(UUID empresa, UUID resultadoId) {
        ResultadoLaboratorio resultado=resultados.findByIdAndEmpresaId(resultadoId,empresa).orElseThrow(() -> new IllegalArgumentException("Resultado no encontrado."));
        if ("COMPLETADA".equals(resultado.getEstado())) throw new IllegalArgumentException("El resultado ya está completado.");
        if ((resultado.getResultado()==null || resultado.getResultado().isBlank()) && resultado.getResultadoNumerico()==null) throw new IllegalArgumentException("No se puede completar un resultado vacío.");
        TenantUserDetails usuario=AuthService.getAuthenticatedUser(); resultado.setEstado("COMPLETADA"); resultado.setValidadoPor(usuario==null?null:usuario.getUsuarioId()); resultado.setValidadoEn(LocalDateTime.now()); resultado.setActualizadoEn(LocalDateTime.now()); resultado=resultados.save(resultado);
        DetalleOrdenEstudio detalle=detalles.findById(resultado.getDetalleOrdenId()).orElseThrow(); detalle.setEstado("COMPLETADA"); detalle.setFechaResultado(LocalDateTime.now().toLocalDate()); detalle.setResultado(resultado.getResultado()!=null?resultado.getResultado():resultado.getResultadoNumerico().toPlainString()); detalle.setObservacionesResultado(resultado.getObservacion()); detalle.setRegistradoPor(resultado.getRegistradoPor()); detalle.setActualizadoEn(LocalDateTime.now()); detalles.save(detalle);
        auditar(empresa,"COMPLETAR_RESULTADO","RESULTADO_LABORATORIO",resultado.getId(),detalle.getEstudio()); return resultado;
    }

    @Transactional
    public ResultadoLaboratorio corregir(UUID empresa, UUID resultadoId, RegistroResultado solicitud, String motivo) {
        if (motivo==null || motivo.isBlank()) throw new IllegalArgumentException("El motivo de corrección es obligatorio.");
        ResultadoLaboratorio anterior=resultados.findByIdAndEmpresaId(resultadoId,empresa).orElseThrow(() -> new IllegalArgumentException("Resultado no encontrado."));
        if (!"VALIDADO".equals(anterior.getEstado())) throw new IllegalArgumentException("Solo se pueden corregir resultados validados.");
        ResultadoLaboratorio nuevo=new ResultadoLaboratorio(); nuevo.setEmpresaId(empresa); nuevo.setOrdenId(anterior.getOrdenId()); nuevo.setDetalleOrdenId(anterior.getDetalleOrdenId()); nuevo.setMuestraId(solicitud.muestraId()==null?anterior.getMuestraId():solicitud.muestraId()); nuevo.setVersion(anterior.getVersion()+1); nuevo.setMotivoCorreccion(motivo.trim()); nuevo.setEstado("BORRADOR");
        nuevo.setResultado(vacio(solicitud.resultado())); nuevo.setResultadoNumerico(solicitud.resultadoNumerico()); nuevo.setUnidad(vacio(solicitud.unidad())); nuevo.setReferenciaMinima(solicitud.referenciaMinima()); nuevo.setReferenciaMaxima(solicitud.referenciaMaxima()); nuevo.setIndicador(indicador(solicitud.resultadoNumerico(),solicitud.referenciaMinima(),solicitud.referenciaMaxima())); nuevo.setObservacion(vacio(solicitud.observacion())); nuevo.setRegistradoEn(LocalDateTime.now()); nuevo.setActualizadoEn(LocalDateTime.now()); TenantUserDetails u=AuthService.getAuthenticatedUser();nuevo.setRegistradoPor(u==null?null:u.getUsuarioId()); nuevo=resultados.save(nuevo);
        DetalleOrdenEstudio detalle=detalles.findById(anterior.getDetalleOrdenId()).orElseThrow(); detalle.setEstado("EN_PROCESO"); detalle.setActualizadoEn(LocalDateTime.now()); detalles.save(detalle); auditar(empresa,"CORREGIR_RESULTADO","RESULTADO_LABORATORIO",nuevo.getId(),motivo); return nuevo;
    }

    public String estadoOrden(UUID empresa, OrdenEstudio orden) {
        List<DetalleOrdenEstudio> estudios=estudiosLaboratorio(orden.getId()); if (estudios.isEmpty()) return "PENDIENTE";
        List<ResultadoLaboratorio> resultadosOrden=resultados.findByEmpresaIdAndOrdenIdOrderByCreadoEnDesc(empresa,orden.getId());
        if (!resultadosOrden.isEmpty() && resultadosOrden.stream().anyMatch(r -> "POR_VALIDAR".equals(r.getEstado()))) return "POR_VALIDAR";
        if (estudios.stream().allMatch(d -> "VALIDADO".equals(d.getEstado()) || "COMPLETADA".equals(d.getEstado()))) return "COMPLETADA";
        if (estudios.stream().anyMatch(d -> "EN_PROCESO".equals(d.getEstado()) || "MUESTRA_TOMADA".equals(d.getEstado()))) return "EN_PROCESO";
        return "PENDIENTE";
    }
    private OrdenEstudio buscarOrden(UUID empresa, UUID id) { return ordenes.findById(id).filter(o -> empresa.equals(o.getEmpresaId())).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada.")); }
    private List<DetalleOrdenEstudio> estudiosLaboratorio(UUID ordenId) { return detalles.findByOrdenId(ordenId).stream().filter(d -> "LABORATORIO".equalsIgnoreCase(d.getTipo())).toList(); }
    private String siguienteCodigoMuestra(UUID empresa) { int n=(int)muestras.count()+1; String codigo; do { codigo="MUE-"+String.format("%06d",n++); } while(muestras.existsByEmpresaIdAndCodigo(empresa,codigo)); return codigo; }
    private String indicador(BigDecimal valor, BigDecimal minimo, BigDecimal maximo) { if(valor==null) return ""; if(minimo!=null&&valor.compareTo(minimo)<0)return "BAJO"; if(maximo!=null&&valor.compareTo(maximo)>0)return "ALTO"; return minimo!=null||maximo!=null?"NORMAL":""; }
    private void auditar(UUID empresa,String accion,String entidad,UUID id,String recurso){TenantUserDetails u=AuthService.getAuthenticatedUser();auditoria.registrar(empresa,u==null?null:u.getUsuarioId(),"LABORATORIO",accion,entidad,id,recurso);}
    private String vacio(String valor){return valor==null||valor.isBlank()?null:valor.trim();} private String normalizar(String valor){return valor==null?"":valor.trim().toUpperCase(Locale.ROOT);}
}
