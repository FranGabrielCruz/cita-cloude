package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import com.citacloud.app.models.Notificacion;
import com.citacloud.app.models.NotificacionDestinatario;
import com.citacloud.app.repositories.NotificacionDestinatarioRepository;
import com.citacloud.app.repositories.NotificacionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class NotificacionService {
    private final NotificacionRepository notificaciones;
    private final NotificacionDestinatarioRepository destinatarios;
    private final NotificacionDestinatarioResolver resolver;
    private final ApplicationEventPublisher eventos;
    private final ConfiguracionFase2Service configuracion;

    public NotificacionService(NotificacionRepository notificaciones, NotificacionDestinatarioRepository destinatarios,
                               NotificacionDestinatarioResolver resolver, ApplicationEventPublisher eventos,
                               ConfiguracionFase2Service configuracion) {
        this.notificaciones = notificaciones;
        this.destinatarios = destinatarios;
        this.resolver = resolver;
        this.eventos = eventos;
        this.configuracion = configuracion;
    }

    @Transactional
    public void crearParaCita(UUID empresaId, String tipo, String categoria, String titulo, String mensaje,
                              Cita cita, UUID medicoAnteriorId) {
        if (cita == null) return;
        Set<UUID> usuarios = resolver.paraCita(empresaId, tipo,
                cita.getMedico() == null ? null : cita.getMedico().getId(), medicoAnteriorId);
        crear(empresaId, tipo, categoria, titulo, mensaje, "CITA", cita.getId(), usuarios);
    }

    /** Para alertas administrativas sin una entidad clínica relacionada. */
    @Transactional
    public void crearEmpresa(UUID empresaId, String tipo, String categoria, String titulo,
                              String mensaje, String entidadTipo, UUID entidadId) {
        crear(empresaId, tipo, categoria, titulo, mensaje, entidadTipo, entidadId,
                resolver.paraAlertaAdministrativa(empresaId));
    }

    private void crear(UUID empresaId, String tipo, String categoria, String titulo, String mensaje,
                       String entidadTipo, UUID entidadId, Collection<UUID> usuarios) {
        if (empresaId == null || !configuracion.obtener(empresaId).isNotificacionesActivas() || usuarios.isEmpty()) return;
        Notificacion notificacion = new Notificacion();
        notificacion.setEmpresaId(empresaId);
        notificacion.setTipo(tipo);
        notificacion.setCategoria(categoria);
        notificacion.setPrioridad("NORMAL");
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setEntidadTipo(entidadTipo);
        notificacion.setEntidadId(entidadId);
        notificacion = notificaciones.save(notificacion);
        Set<UUID> unicos = new java.util.LinkedHashSet<>(usuarios);
        for (UUID usuarioId : unicos) {
            if (usuarioId == null) continue;
            NotificacionDestinatario destinatario = new NotificacionDestinatario();
            destinatario.setNotificacion(notificacion);
            destinatario.setUsuarioId(usuarioId);
            destinatarios.save(destinatario);
        }
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId, unicos));
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listar(UUID empresaId, UUID usuarioId) {
        return destinatarios.listarParaUsuario(empresaId, usuarioId).stream().map(destinatario -> {
            Notificacion notificacion = destinatario.getNotificacion();
            notificacion.setLeida(destinatario.isLeida());
            return notificacion;
        }).toList();
    }

    public long sinLeer(UUID empresaId, UUID usuarioId) {
        if (empresaId == null || usuarioId == null) return 0;
        return destinatarios.listarParaUsuario(empresaId, usuarioId).stream().filter(d -> !d.isLeida()).count();
    }

    @Transactional
    public void leer(UUID empresaId, UUID usuarioId, UUID notificacionId) {
        destinatarios.buscarDestinatario(empresaId, usuarioId, notificacionId).ifPresent(this::marcarLeida);
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId, Set.of(usuarioId)));
    }

    @Transactional
    public void leerTodas(UUID empresaId, UUID usuarioId) {
        destinatarios.listarParaUsuario(empresaId, usuarioId).stream().filter(d -> !d.isLeida()).forEach(this::marcarLeida);
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId, Set.of(usuarioId)));
    }

    private void marcarLeida(NotificacionDestinatario destinatario) {
        destinatario.setLeida(true);
        destinatario.setLeidaEn(LocalDateTime.now());
        destinatarios.save(destinatario);
    }
}
