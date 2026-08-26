package com.citacloud.app.services;

import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Permiso;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Resuelve destinatarios en backend según el tipo de evento y su contexto. */
@Service
public class NotificacionDestinatarioResolver {
    private final UsuarioRepository usuarios;
    private final MedicoRepository medicos;

    public NotificacionDestinatarioResolver(UsuarioRepository usuarios, MedicoRepository medicos) {
        this.usuarios = usuarios;
        this.medicos = medicos;
    }

    public Set<UUID> paraCita(UUID empresaId, String tipo, UUID medicoId, UUID medicoAnteriorId) {
        Set<UUID> destinatarios = administradores(empresaId);
        usuarioDelMedico(empresaId, medicoId).ifPresent(destinatarios::add);
        usuarioDelMedico(empresaId, medicoAnteriorId).ifPresent(destinatarios::add);
        if (eventoOperativo(tipo)) destinatarios.addAll(usuariosConRol(empresaId, "RECEPCION", "RECEPCIONISTA", "SECRETARIA"));
        if ("PACIENTE_EN_ESPERA".equals(tipo)) destinatarios.addAll(usuariosConRol(empresaId, "ENFERMERIA", "ENFERMERA"));
        return destinatarios;
    }

    public Set<UUID> paraAlertaAdministrativa(UUID empresaId) { return administradores(empresaId); }

    /** Los movimientos financieros solo son visibles para administración y personal financiero del tenant. */
    public Set<UUID> paraPago(UUID empresaId) {
        Set<UUID> resultado = new LinkedHashSet<>();
        for (Usuario usuario : usuarios.findByEmpresaId(empresaId)) {
            if (!Boolean.TRUE.equals(usuario.getActivo()) || esMedico(usuario)) continue;
            if (esAdministrador(usuario) || tieneRolFinanciero(usuario) || tienePermisoDePago(usuario)) {
                resultado.add(usuario.getId());
            }
        }
        return resultado;
    }

    private Set<UUID> administradores(UUID empresaId) { return usuariosConRol(empresaId, "ADMINISTRADOR", "SUPERADMIN"); }

    private boolean esAdministrador(Usuario usuario) { return tieneRol(usuario, "ADMINISTRADOR", "SUPERADMIN"); }

    private boolean esMedico(Usuario usuario) { return tieneRol(usuario, "MEDICO", "MÉDICO"); }

    private boolean tieneRolFinanciero(Usuario usuario) {
        return tieneRol(usuario, "FINANZAS", "FINANCIERO", "CAJA", "CAJERO", "CONTABILIDAD", "TESORERIA", "TESORERÍA");
    }

    private boolean tienePermisoDePago(Usuario usuario) {
        return usuario.getRoles().stream().flatMap(rol -> rol.getPermisos().stream())
                .map(Permiso::getCodigo).anyMatch(codigo -> codigo != null && codigo.startsWith("PAYMENTS_"));
    }

    private boolean tieneRol(Usuario usuario, String... roles) {
        return usuario.getRoles().stream().map(Rol::getNombre).map(this::normalizar)
                .anyMatch(rol -> java.util.Arrays.stream(roles).map(this::normalizar).anyMatch(rol::equals));
    }

    private Set<UUID> usuariosConRol(UUID empresaId, String... roles) {
        Set<UUID> resultado = new LinkedHashSet<>();
        for (Usuario usuario : usuarios.findByEmpresaId(empresaId)) {
            if (!Boolean.TRUE.equals(usuario.getActivo())) continue;
            boolean coincide = tieneRol(usuario, roles);
            if (coincide) resultado.add(usuario.getId());
        }
        return resultado;
    }

    private java.util.Optional<UUID> usuarioDelMedico(UUID empresaId, UUID medicoId) {
        if (medicoId == null) return java.util.Optional.empty();
        return medicos.findByIdAndEmpresaId(medicoId, empresaId).map(medico -> medico.getUsuarioId());
    }

    private boolean eventoOperativo(String tipo) {
        return Set.of("CITA_PENDIENTE", "CITA_CONFIRMADA", "CITA_CANCELADA", "CITA_APROBADA",
                "CITA_REPROGRAMADA", "CITA_REASIGNADA", "PACIENTE_EN_ESPERA").contains(tipo);
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase();
    }
}
