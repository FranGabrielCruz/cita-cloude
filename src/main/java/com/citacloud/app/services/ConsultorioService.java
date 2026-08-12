package com.citacloud.app.services;

import com.citacloud.app.models.Consultorio;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.repositories.ConsultorioRepository;
import com.citacloud.app.repositories.SucursalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConsultorioService {

    private static final Pattern CODIGO_CONSULTORIO = Pattern.compile("^CONS(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final ConsultorioRepository consultorioRepository;
    private final SucursalRepository sucursalRepository;

    public ConsultorioService(ConsultorioRepository consultorioRepository, SucursalRepository sucursalRepository) {
        this.consultorioRepository = consultorioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public List<Consultorio> listarPorEmpresa(UUID empresaId) {
        return consultorioRepository.findByEmpresaId(empresaId);
    }

    public List<Consultorio> listarActivos(UUID empresaId) {
        return consultorioRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    public Consultorio guardar(Consultorio consultorio) {
        return consultorioRepository.save(consultorio);
    }

    public List<Consultorio> buscar(UUID empresaId, String codigo, String nombre, UUID sucursalId, Boolean activo) {
        String codigoNormalizado = normalizar(codigo);
        String nombreNormalizado = normalizar(nombre);
        return listarPorEmpresa(empresaId).stream()
                .filter(consultorio -> codigoNormalizado.isBlank()
                        || consultorio.getCodigo().toLowerCase(Locale.ROOT).contains(codigoNormalizado))
                .filter(consultorio -> nombreNormalizado.isBlank()
                        || consultorio.getNombre().toLowerCase(Locale.ROOT).contains(nombreNormalizado))
                .filter(consultorio -> sucursalId == null
                        || (consultorio.getSucursal() != null && sucursalId.equals(consultorio.getSucursal().getId())))
                .filter(consultorio -> activo == null || activo.equals(consultorio.getActivo()))
                .toList();
    }

    public Consultorio crear(UUID empresaId, String nombre, UUID sucursalId, String ubicacion) {
        Consultorio consultorio = new Consultorio();
        consultorio.setEmpresaId(empresaId);
        actualizarDatos(consultorio, empresaId, siguienteCodigo(empresaId), nombre, sucursalId, ubicacion);
        consultorio.setActivo(true);
        return consultorioRepository.save(consultorio);
    }

    public Consultorio actualizar(UUID empresaId, UUID id, String codigo, String nombre, UUID sucursalId, String ubicacion) {
        Consultorio consultorio = obtener(empresaId, id);
        actualizarDatos(consultorio, empresaId, codigo, nombre, sucursalId, ubicacion);
        return consultorioRepository.save(consultorio);
    }

    public Consultorio cambiarEstado(UUID empresaId, UUID id, boolean activo) {
        Consultorio consultorio = obtener(empresaId, id);
        consultorio.setActivo(activo);
        return consultorioRepository.save(consultorio);
    }

    private void actualizarDatos(Consultorio consultorio, UUID empresaId, String codigo, String nombre, UUID sucursalId,
                                String ubicacion) {
        String codigoLimpio = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (codigoLimpio.isBlank() || nombreLimpio.isBlank() || sucursalId == null) {
            throw new IllegalArgumentException("C\u00f3digo, nombre y sucursal son obligatorios.");
        }
        Sucursal sucursal = sucursalRepository.findByIdAndEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La sucursal seleccionada no pertenece a la empresa."));
        boolean duplicado = consultorioRepository.findByEmpresaIdAndSucursalId(empresaId, sucursalId).stream()
                .anyMatch(item -> item.getCodigo().equalsIgnoreCase(codigoLimpio)
                        && !item.getId().equals(consultorio.getId()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un consultorio con ese c\u00f3digo en la sucursal seleccionada.");
        }
        consultorio.setCodigo(codigoLimpio);
        consultorio.setNombre(nombreLimpio);
        consultorio.setSucursal(sucursal);
        consultorio.setUbicacion(ubicacion == null || ubicacion.isBlank() ? null : ubicacion.trim());
    }

    private Consultorio obtener(UUID empresaId, UUID id) {
        return consultorioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontr\u00f3 el consultorio solicitado."));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String siguienteCodigo(UUID empresaId) {
        int siguiente = listarPorEmpresa(empresaId).stream()
                .map(Consultorio::getCodigo)
                .map(CODIGO_CONSULTORIO::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0) + 1;
        return String.format(Locale.ROOT, "CONS%02d", siguiente);
    }
}
