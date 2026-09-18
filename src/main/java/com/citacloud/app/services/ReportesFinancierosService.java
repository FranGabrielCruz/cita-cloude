package com.citacloud.app.services;

import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Capa de consulta: nunca crea ni modifica transacciones financieras. */
@Service
@Transactional(readOnly = true)
public class ReportesFinancierosService {
    private final FacturaRepository facturas; private final PagoRepository pagos; private final CargoFinancieroRepository cargos;
    private final ReembolsoPagoRepository reembolsos; private final SesionCajaRepository sesiones; private final MovimientoCajaRepository movimientos;
    private final DetalleFacturaRepository detalles;
    public ReportesFinancierosService(FacturaRepository facturas, PagoRepository pagos, CargoFinancieroRepository cargos,
            ReembolsoPagoRepository reembolsos, SesionCajaRepository sesiones, MovimientoCajaRepository movimientos, DetalleFacturaRepository detalles) {
        this.facturas=facturas; this.pagos=pagos; this.cargos=cargos; this.reembolsos=reembolsos; this.sesiones=sesiones; this.movimientos=movimientos; this.detalles=detalles;
    }
    public record Filtro(LocalDate desde, LocalDate hasta, UUID sucursalId) {
        public Filtro { if (desde==null || hasta==null) throw new IllegalArgumentException("El período es obligatorio."); if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha desde no puede ser posterior a hasta."); }
    }
    public record Resumen(BigDecimal facturado,long facturas,BigDecimal cobrado,long pagos,BigDecimal porCobrar,long cuentas,BigDecimal reembolsado,long reembolsos) {}
    public record Metodo(String nombre, BigDecimal monto, long cantidad, BigDecimal porcentaje) {}
    public record ServicioFacturado(String nombre, BigDecimal cantidad, BigDecimal facturado, BigDecimal porcentaje) {}
    public record CajaResumen(String caja, long aperturas, BigDecimal ingresos, BigDecimal egresos, BigDecimal diferencia) {}
    public record SerieTemporal(LocalDate fecha, BigDecimal facturado, BigDecimal cobrado) {}
    public Resumen resumen(UUID empresa, Filtro filtro) { validarAcceso(empresa);
        List<Factura> fs=facturas(empresa,filtro); List<Pago> ps=pagos(empresa,filtro); List<ReembolsoPago> rs=reembolsos(empresa,filtro);
        List<CargoFinanciero> cs=cargos.findByEmpresaIdOrderByFechaDesc(empresa).stream().filter(sucursalCargo(filtro)).filter(c->n(c.getSaldo()).signum()>0).toList();
        return new Resumen(suma(fs.stream().map(Factura::getTotal).toList()),fs.size(),suma(ps.stream().map(Pago::getMonto).toList()),ps.size(),suma(cs.stream().map(CargoFinanciero::getSaldo).toList()),cs.size(),suma(rs.stream().map(ReembolsoPago::getMonto).toList()),rs.size());
    }
    public List<Factura> facturas(UUID empresa, Filtro f) { validarAcceso(empresa); return facturas.findByEmpresaIdOrderByFechaDesc(empresa).stream().filter(x->validaFactura(x)).filter(x->enPeriodo(x.getFecha(),f)).toList(); }
    public List<Pago> pagos(UUID empresa, Filtro f) { validarAcceso(empresa); return pagos.findByEmpresaIdOrderByCreadoEnDesc(empresa).stream().filter(x->!"VOIDED".equals(x.getEstado())).filter(x->enPeriodo(x.getFecha(),f)).filter(sucursal(f)).toList(); }
    public List<CargoFinanciero> cuentas(UUID empresa, Filtro f) { validarAcceso(empresa); return cargos.findByEmpresaIdOrderByFechaDesc(empresa).stream().filter(x->n(x.getSaldo()).signum()>0).filter(sucursalCargo(f)).toList(); }
    public List<ReembolsoPago> reembolsos(UUID empresa, Filtro f) { validarAcceso(empresa); return reembolsos.findByEmpresaIdOrderByReembolsadoEnDesc(empresa).stream().filter(x->enPeriodo(x.getReembolsadoEn()==null?null:x.getReembolsadoEn().toLocalDate(),f)).filter(x->f.sucursalId()==null || (x.getPago()!=null && x.getPago().getSucursal()!=null && f.sucursalId().equals(x.getPago().getSucursal().getId()))).toList(); }
    public List<Metodo> porMetodo(UUID empresa, Filtro f) { List<Pago> lista=pagos(empresa,f); BigDecimal total=suma(lista.stream().map(Pago::getMonto).toList()); return lista.stream().collect(Collectors.groupingBy(x->texto(x.getMetodoPago()))).entrySet().stream().map(e->new Metodo(e.getKey(),suma(e.getValue().stream().map(Pago::getMonto).toList()),e.getValue().size(),porcentaje(suma(e.getValue().stream().map(Pago::getMonto).toList()),total))).sorted(Comparator.comparing(Metodo::monto).reversed()).toList(); }
    public List<ServicioFacturado> servicios(UUID empresa, Filtro f) { BigDecimal total=resumen(empresa,f).facturado(); Set<UUID> ids=facturas(empresa,f).stream().map(Factura::getId).collect(Collectors.toSet()); return detalles.findByEmpresaId(empresa).stream().filter(d->d.getFactura()!=null&&ids.contains(d.getFactura().getId())).collect(Collectors.groupingBy(d->texto(d.getDescripcion()))).entrySet().stream().map(e->{BigDecimal importe=suma(e.getValue().stream().map(DetalleFactura::getImporte).toList()); return new ServicioFacturado(e.getKey(),suma(e.getValue().stream().map(DetalleFactura::getCantidad).toList()),importe,porcentaje(importe,total));}).sorted(Comparator.comparing(ServicioFacturado::facturado).reversed()).toList(); }
    public List<CajaResumen> cajas(UUID empresa, Filtro f) { return sesiones.findByEmpresaIdOrderByAbiertoEnDesc(empresa).stream().filter(s->enPeriodo(s.getAbiertoEn()==null?null:s.getAbiertoEn().toLocalDate(),f)).filter(s->f.sucursalId()==null || (s.getSucursal()!=null&&f.sucursalId().equals(s.getSucursal().getId()))).map(s->{List<MovimientoCaja> ms=movimientos.findByEmpresaIdAndSesionIdOrderByCreadoEnAsc(empresa,s.getId());BigDecimal in=suma(ms.stream().filter(m->"IN".equals(m.getDireccion())).map(MovimientoCaja::getMonto).toList());BigDecimal out=suma(ms.stream().filter(m->"OUT".equals(m.getDireccion())).map(MovimientoCaja::getMonto).toList());return new CajaResumen(s.getCaja()==null?"Caja":s.getCaja().getNombre(),1,in,out,n(s.getDiferencia()));}).toList(); }
    public List<SerieTemporal> serieTemporal(UUID empresa, Filtro f) { validarAcceso(empresa); Map<LocalDate,BigDecimal> facturado=facturas(empresa,f).stream().collect(Collectors.groupingBy(Factura::getFecha,TreeMap::new,Collectors.reducing(BigDecimal.ZERO,Factura::getTotal,BigDecimal::add))); Map<LocalDate,BigDecimal> cobrado=pagos(empresa,f).stream().collect(Collectors.groupingBy(Pago::getFecha,TreeMap::new,Collectors.reducing(BigDecimal.ZERO,Pago::getMonto,BigDecimal::add))); List<SerieTemporal> serie=new ArrayList<>();for(LocalDate d=f.desde();!d.isAfter(f.hasta());d=d.plusDays(1))serie.add(new SerieTemporal(d,n(facturado.get(d)),n(cobrado.get(d))));return serie; }
    private void validarAcceso(UUID empresa){TenantUserDetails u=AuthService.getAuthenticatedUser();if(u==null||empresa==null||!empresa.equals(u.getEmpresaId()))throw new IllegalArgumentException("No tiene permisos para consultar reportes financieros.");boolean permitido=u.getAuthorities().stream().anyMatch(a->"MENU_REPORTES_FINANCIEROS".equals(a.getAuthority())||"ROLE_ADMINISTRADOR".equals(a.getAuthority())||"ROLE_SUPERADMIN".equals(a.getAuthority()));if(!permitido)throw new IllegalArgumentException("No tiene permisos para consultar reportes financieros.");}
    private boolean validaFactura(Factura f){ return f!=null && !"ANULADA".equals(f.getEstado()) && !"BORRADOR".equals(f.getEstado()); }
    private boolean enPeriodo(LocalDate fecha,Filtro f){ return fecha!=null&&!fecha.isBefore(f.desde())&&!fecha.isAfter(f.hasta()); }
    private Predicate<Pago> sucursal(Filtro f){ return p->f.sucursalId()==null || (p.getSucursal()!=null&&f.sucursalId().equals(p.getSucursal().getId())); }
    private Predicate<CargoFinanciero> sucursalCargo(Filtro f){ return c->f.sucursalId()==null || (c.getSucursal()!=null&&f.sucursalId().equals(c.getSucursal().getId())); }
    private static BigDecimal n(BigDecimal v){return v==null?BigDecimal.ZERO:v;} private static BigDecimal suma(Collection<BigDecimal> v){return v.stream().map(ReportesFinancierosService::n).reduce(BigDecimal.ZERO,BigDecimal::add);} private static BigDecimal porcentaje(BigDecimal parte,BigDecimal total){return total.signum()==0?BigDecimal.ZERO:parte.multiply(BigDecimal.valueOf(100)).divide(total,2,RoundingMode.HALF_UP);} private static String texto(String v){return v==null||v.isBlank()?"Sin especificar":v;}
}
