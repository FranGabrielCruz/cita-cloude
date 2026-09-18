package com.citacloud.app.services;

import com.citacloud.app.models.SecuenciaComprobanteFiscal;
import com.citacloud.app.repositories.SecuenciaComprobanteFiscalRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SecuenciaComprobanteFiscalService {
    public record Asignacion(SecuenciaComprobanteFiscal secuencia, String numero) { }
    public static final Map<String, String> TIPOS = Map.of(
            "CONSUMIDOR_FINAL", "Consumidor final",
            "CREDITO_FISCAL", "Crédito fiscal",
            "GUBERNAMENTAL", "Gubernamental",
            "REGIMEN_ESPECIAL", "Régimen especial");

    private final SecuenciaComprobanteFiscalRepository repositorio;

    public SecuenciaComprobanteFiscalService(SecuenciaComprobanteFiscalRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<SecuenciaComprobanteFiscal> listar(UUID empresaId) {
        validarEmpresa(empresaId);
        return repositorio.findByEmpresaIdOrderByNombre(empresaId);
    }

    @Transactional(readOnly = true)
    public List<SecuenciaComprobanteFiscal> listarActivas(UUID empresaId) {
        validarEmpresa(empresaId);
        return repositorio.findByEmpresaIdAndActivaTrueOrderByNombre(empresaId);
    }

    @Transactional
    public SecuenciaComprobanteFiscal guardar(UUID empresaId, UUID id, String tipo, String prefijo,
                                               Long desde, Long hasta, Boolean activa) {
        requerirConfiguracion(empresaId);
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!TIPOS.containsKey(tipoNormalizado)) throw new IllegalArgumentException("Seleccione un tipo de comprobante fiscal válido.");
        String prefijoNormalizado = prefijo == null ? "" : prefijo.trim().toUpperCase(Locale.ROOT);
        if (prefijoNormalizado.isBlank()) throw new IllegalArgumentException("El prefijo fiscal es obligatorio.");
        if (desde == null || hasta == null || desde < 0 || hasta < desde) {
            throw new IllegalArgumentException("El rango fiscal desde/hasta no es válido.");
        }
        UUID identificador = id == null ? new UUID(0, 0) : id;
        if (repositorio.existsByEmpresaIdAndTipoAndIdNot(empresaId, tipoNormalizado, identificador)) {
            throw new IllegalArgumentException("Ya existe una secuencia para este tipo de comprobante fiscal.");
        }
        SecuenciaComprobanteFiscal secuencia = id == null ? new SecuenciaComprobanteFiscal()
                : repositorio.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new IllegalArgumentException("Secuencia fiscal no encontrada."));
        if (secuencia.getId() == null) {
            secuencia.setEmpresaId(empresaId);
            secuencia.setNumeroSiguiente(desde);
        } else if (secuencia.getNumeroSiguiente() < desde) {
            secuencia.setNumeroSiguiente(desde);
        }
        if (secuencia.getNumeroSiguiente() > hasta + 1) {
            throw new IllegalArgumentException("El límite final no puede quedar por debajo de los comprobantes ya utilizados.");
        }
        secuencia.setTipo(tipoNormalizado);
        secuencia.setNombre(TIPOS.get(tipoNormalizado));
        secuencia.setPrefijo(prefijoNormalizado);
        secuencia.setNumeroDesde(desde);
        secuencia.setNumeroHasta(hasta);
        secuencia.setActiva(Boolean.TRUE.equals(activa));
        return repositorio.save(secuencia);
    }

    @Transactional
    public Asignacion consumirAsignacion(UUID empresaId, String tipo) {
        if (tipo == null || tipo.isBlank()) return null;
        SecuenciaComprobanteFiscal encontrada = repositorio.findByEmpresaIdAndTipoAndActivaTrue(empresaId, tipo)
                .orElseThrow(() -> new IllegalArgumentException("Configure y active la secuencia de " + etiqueta(tipo) + " antes de emitir."));
        SecuenciaComprobanteFiscal secuencia = repositorio.bloquear(encontrada.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La secuencia fiscal ya no está disponible."));
        long siguiente = secuencia.getNumeroSiguiente();
        if (siguiente > secuencia.getNumeroHasta()) {
            throw new IllegalArgumentException("La secuencia de " + secuencia.getNombre() + " está agotada.");
        }
        secuencia.setNumeroSiguiente(siguiente + 1);
        repositorio.save(secuencia);
        return new Asignacion(secuencia, secuencia.getPrefijo() + String.format(Locale.ROOT, "%08d", siguiente));
    }

    public static String etiqueta(String tipo) {
        if (tipo == null || tipo.isBlank()) return "Sin comprobante fiscal";
        return TIPOS.getOrDefault(tipo, tipo.replace('_', ' '));
    }

    private void validarEmpresa(UUID empresaId) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (empresaId == null || usuario == null || !empresaId.equals(usuario.getEmpresaId())) {
            throw new IllegalArgumentException("No existe una empresa autenticada.");
        }
    }

    private void requerirConfiguracion(UUID empresaId) {
        validarEmpresa(empresaId);
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        boolean permitido = usuario.getAuthorities().stream().anyMatch(a -> Set.of(
                "ROLE_ADMINISTRADOR", "ROLE_SUPERADMIN", "BILLING_FISCAL_CONFIG").contains(a.getAuthority()));
        if (!permitido) throw new IllegalArgumentException("No tiene permiso para configurar secuencias fiscales.");
    }
}
