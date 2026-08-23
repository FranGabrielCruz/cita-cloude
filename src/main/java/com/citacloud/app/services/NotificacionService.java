package com.citacloud.app.services;

import com.citacloud.app.models.Notificacion;
import com.citacloud.app.repositories.NotificacionRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificacionService {
    private final NotificacionRepository repositorio;
    private final UsuarioRepository usuarios;
    private final ApplicationEventPublisher eventos;

    public NotificacionService(NotificacionRepository repositorio, UsuarioRepository usuarios,
                               ApplicationEventPublisher eventos) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    @Transactional
    public void crearEmpresa(UUID empresaId, String tipo, String categoria, String titulo,
                              String mensaje, String entidadTipo, UUID entidadId) {
        usuarios.findByEmpresaId(empresaId).stream().filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .forEach(usuario -> {
                    Notificacion notificacion = new Notificacion();
                    notificacion.setEmpresaId(empresaId);
                    notificacion.setUsuarioId(usuario.getId());
                    notificacion.setTipo(tipo);
                    notificacion.setCategoria(categoria);
                    notificacion.setPrioridad("NORMAL");
                    notificacion.setTitulo(titulo);
                    notificacion.setMensaje(mensaje);
                    notificacion.setEntidadTipo(entidadTipo);
                    notificacion.setEntidadId(entidadId);
                    repositorio.save(notificacion);
                });
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId));
    }

    public List<Notificacion> listar(UUID empresaId, UUID usuarioId) {
        return repositorio.findByEmpresaIdAndUsuarioIdOrderByCreadaEnDesc(empresaId, usuarioId);
    }

    public long sinLeer(UUID empresaId, UUID usuarioId) {
        return repositorio.countByEmpresaIdAndUsuarioIdAndLeidaFalse(empresaId, usuarioId);
    }

    @Transactional
    public void leer(UUID empresaId, UUID usuarioId, UUID notificacionId) {
        repositorio.findById(notificacionId).filter(notificacion -> empresaId.equals(notificacion.getEmpresaId())
                        && usuarioId.equals(notificacion.getUsuarioId()))
                .ifPresent(notificacion -> marcarLeida(notificacion));
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId));
    }

    @Transactional
    public void leerTodas(UUID empresaId, UUID usuarioId) {
        listar(empresaId, usuarioId).stream().filter(notificacion -> !notificacion.isLeida())
                .forEach(this::marcarLeida);
        eventos.publishEvent(new NotificacionesCreadasEvent(empresaId));
    }

    private void marcarLeida(Notificacion notificacion) {
        notificacion.setLeida(true);
        notificacion.setLeidaEn(LocalDateTime.now());
        repositorio.save(notificacion);
    }
}
