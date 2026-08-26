package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import com.citacloud.app.repositories.CitaRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Fuente única de los cálculos para la pantalla y los documentos de Gestión y Control. */
@Service
public class ManagementControlReportService {
    public record Filtros(LocalDate desde, LocalDate hasta, UUID sucursalId, UUID medicoId, UUID especialidadId) {}
    public record Kpi(String nombre, String valor, Double variacion, boolean favorable) {}
    public record Productividad(String medico, long citas, long atendidas, long canceladas, long noShow, double promedioDia) {}
    public record EstadoCita(String estado, long cantidad, double porcentaje) {}
    public record EspecialidadResumen(String especialidad, long citas, long atendidas, double porcentaje) {}
    public record IndicadorControl(String indicador, String resultado, String meta, String estado) {}
    public record TiemposAtencion(String espera, String consulta, String total) {}
    public record Reporte(List<Kpi> resumen, List<Productividad> productividad, long pacientesAtendidos,
                          long pacientesNuevos, long pacientesRecurrentes, long totalCitas, List<EstadoCita> estados,
                          List<EspecialidadResumen> especialidades, TiemposAtencion tiempos, List<IndicadorControl> indicadores, List<String> alertas) {}

    private final CitaRepository citas;
    public ManagementControlReportService(CitaRepository citas) { this.citas = citas; }

    @Transactional(readOnly = true)
    public Reporte generar(UUID empresaId, Filtros filtros) {
        validarAcceso(empresaId);
        if (filtros == null || filtros.desde() == null || filtros.hasta() == null || filtros.desde().isAfter(filtros.hasta()))
            throw new IllegalArgumentException("El período seleccionado no es válido.");
        List<Cita> actual = filtrar(citas.findByEmpresaIdAndFechaBetween(empresaId, filtros.desde(), filtros.hasta()), filtros);
        long dias = ChronoUnit.DAYS.between(filtros.desde(), filtros.hasta()) + 1;
        LocalDate anteriorHasta = filtros.desde().minusDays(1);
        Filtros anterior = new Filtros(anteriorHasta.minusDays(dias - 1), anteriorHasta, filtros.sucursalId(), filtros.medicoId(), filtros.especialidadId());
        List<Cita> previo = filtrar(citas.findByEmpresaIdAndFechaBetween(empresaId, anterior.desde(), anterior.hasta()), anterior);
        long total = actual.size(), atendidas = contar(actual, "ATENDIDA"), canceladas = contar(actual, "CANCELADA"), noShow = contar(actual, "NO_ASISTIO");
        long atendidasPrevias = contar(previo, "ATENDIDA"), noShowPrevio = contar(previo, "NO_ASISTIO");
        long pacientesAtendidos = actual.stream().filter(c -> "ATENDIDA".equals(c.getEstado())).map(c -> c.getPaciente().getId()).distinct().count();
        Set<UUID> atendidosAntes = citas.findByEmpresaId(empresaId).stream().filter(c -> "ATENDIDA".equals(c.getEstado()) && c.getFecha().isBefore(filtros.desde())).map(c -> c.getPaciente().getId()).collect(Collectors.toSet());
        long nuevos = actual.stream().filter(c -> "ATENDIDA".equals(c.getEstado())).map(c -> c.getPaciente().getId()).filter(id -> !atendidosAntes.contains(id)).distinct().count();
        double asistencia = porcentaje(atendidas, atendidas + noShow);
        double asistenciaPrevia = porcentaje(atendidasPrevias, atendidasPrevias + noShowPrevio);
        List<Kpi> resumen = List.of(
                kpi("Citas", total, (long) previo.size(), true, ""), kpi("Atendidos", atendidas, atendidasPrevias, true, ""),
                kpi("Canceladas", canceladas, contar(previo, "CANCELADA"), false, ""), kpi("No-show", noShow, noShowPrevio, false, ""),
                kpi("Pacientes nuevos", nuevos, 0, true, ""), kpi("Espera prom.", null, null, true, "Sin datos"),
                kpi("Asistencia", asistencia, asistenciaPrevia, true, "%"), kpi("Ocupación", null, null, true, "Sin datos"));
        Map<String, List<Cita>> porMedico = actual.stream().collect(Collectors.groupingBy(c -> c.getMedico().getNombreCompleto(), LinkedHashMap::new, Collectors.toList()));
        List<Productividad> productividad = porMedico.entrySet().stream().map(e -> new Productividad(e.getKey(), e.getValue().size(), contar(e.getValue(), "ATENDIDA"), contar(e.getValue(), "CANCELADA"), contar(e.getValue(), "NO_ASISTIO"), redondear((double) e.getValue().size() / dias))).sorted(Comparator.comparing(Productividad::citas).reversed()).toList();
        validarConsistencia(total, atendidas, canceladas, noShow, productividad);
        List<EstadoCita> estados=actual.stream().collect(Collectors.groupingBy(Cita::getEstado,TreeMap::new,Collectors.counting())).entrySet().stream().map(e->new EstadoCita(nombreEstado(e.getKey()),e.getValue(),porcentaje(e.getValue(),total))).toList();
        Map<String,List<Cita>> porEspecialidad=new TreeMap<>(); for(Cita cita:actual) for(var esp:cita.getMedico().getEspecialidades()) porEspecialidad.computeIfAbsent(esp.getNombre(),x->new ArrayList<>()).add(cita);
        List<EspecialidadResumen> resumenEspecialidades=porEspecialidad.entrySet().stream().map(e->new EspecialidadResumen(e.getKey(),e.getValue().size(),contar(e.getValue(),"ATENDIDA"),porcentaje(e.getValue().size(),total))).toList();
        TiemposAtencion tiempos=new TiemposAtencion("—","—","—");
        List<IndicadorControl> indicadores=List.of(new IndicadorControl("Tasa de asistencia",String.format(Locale.ROOT,"%.1f%%",asistencia),"≥ 85%",asistencia>=85?"Cumple":"Atención"),new IndicadorControl("Cancelaciones",String.format(Locale.ROOT,"%.1f%%",porcentaje(canceladas,total)),"≤ 10%",porcentaje(canceladas,total)<=10?"Cumple":"Atención"),new IndicadorControl("No-show",String.format(Locale.ROOT,"%.1f%%",porcentaje(noShow,total)),"≤ 5%",porcentaje(noShow,total)<=5?"Cumple":"Atención"));
        List<String> alertas=indicadores.stream().map(i->("Cumple".equals(i.estado())?"✓ ":"⚠ ")+i.indicador()+" "+("Cumple".equals(i.estado())?"dentro de la meta.":"requiere atención.")).toList();
        return new Reporte(resumen, productividad, pacientesAtendidos, nuevos, Math.max(0, pacientesAtendidos - nuevos), total, estados, resumenEspecialidades, tiempos, indicadores, alertas);
    }
    @Transactional(readOnly = true)
    public List<Cita> citasDelReporte(UUID empresaId, Filtros filtros) { validarAcceso(empresaId); if(filtros==null||filtros.desde()==null||filtros.hasta()==null||filtros.desde().isAfter(filtros.hasta())) throw new IllegalArgumentException("El período seleccionado no es válido."); return filtrar(citas.findByEmpresaIdAndFechaBetween(empresaId,filtros.desde(),filtros.hasta()),filtros); }

    private List<Cita> filtrar(List<Cita> datos, Filtros f) { return datos.stream().filter(c -> f.sucursalId() == null || f.sucursalId().equals(c.getSucursal().getId())).filter(c -> f.medicoId() == null || f.medicoId().equals(c.getMedico().getId())).filter(c -> f.especialidadId() == null || c.getMedico().getEspecialidades().stream().anyMatch(e -> f.especialidadId().equals(e.getId()))).toList(); }
    private long contar(List<Cita> datos, String estado) { return datos.stream().filter(c -> estado.equals(c.getEstado())).count(); }
    private double porcentaje(long numerador, long denominador) { return denominador == 0 ? 0 : redondear(numerador * 100d / denominador); }
    private double redondear(double valor) { return Math.round(valor * 10d) / 10d; }
    private String nombreEstado(String estado){return switch(estado){case "ATENDIDA"->"Atendidas";case "CANCELADA"->"Canceladas";case "NO_ASISTIO"->"No-show";case "PENDIENTE"->"Pendientes";case "CONFIRMADA"->"Confirmadas";case "EN_ESPERA"->"En espera";case "EN_CONSULTA"->"En consulta";default->estado;};}
    /** La tabla de productividad y el resumen comparten el mismo universo filtrado. */
    private void validarConsistencia(long total, long atendidas, long canceladas, long noShow, List<Productividad> productividad) { long citasTabla=productividad.stream().mapToLong(Productividad::citas).sum(), atendidosTabla=productividad.stream().mapToLong(Productividad::atendidas).sum(), canceladasTabla=productividad.stream().mapToLong(Productividad::canceladas).sum(), noShowTabla=productividad.stream().mapToLong(Productividad::noShow).sum(); if(total!=citasTabla||atendidas!=atendidosTabla||canceladas!=canceladasTabla||noShow!=noShowTabla) throw new IllegalStateException("Inconsistencia interna al calcular el reporte de gestión y control."); }
    private Kpi kpi(String nombre, Number actual, Number previo, boolean mayorEsMejor, String sufijo) { if (actual == null) return new Kpi(nombre, "—", null, mayorEsMejor); double a = actual.doubleValue(), p = previo == null ? 0 : previo.doubleValue(); Double variacion = p == 0 ? null : redondear((a - p) * 100 / p); String valor = sufijo.equals("%") ? String.format(Locale.ROOT, "%.1f%%", a) : String.valueOf(actual.longValue()); return new Kpi(nombre, valor, variacion, mayorEsMejor); }
    private void validarAcceso(UUID empresaId) { TenantUserDetails u = AuthService.getAuthenticatedUser(); if (u == null || empresaId == null || !empresaId.equals(u.getEmpresaId())) throw new IllegalArgumentException("No tienes permiso para consultar este reporte."); boolean permitido = u.getAuthorities().stream().anyMatch(a -> "reports.management.view".equals(a.getAuthority()) || "ROLE_ADMINISTRADOR".equals(a.getAuthority()) || "ROLE_SUPERADMIN".equals(a.getAuthority())); if (!permitido) throw new IllegalArgumentException("No tienes permiso para consultar este reporte."); }
}
