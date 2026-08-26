package com.citacloud.app.repositories;

import com.citacloud.app.models.NotificacionDestinatario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacionDestinatarioRepository extends JpaRepository<NotificacionDestinatario, UUID> {
    @Query("select d from NotificacionDestinatario d join fetch d.notificacion n where n.empresaId = :empresaId and d.usuarioId = :usuarioId order by n.creadaEn desc")
    List<NotificacionDestinatario> listarParaUsuario(@Param("empresaId") UUID empresaId, @Param("usuarioId") UUID usuarioId);
    @Query("select d from NotificacionDestinatario d join fetch d.notificacion n where n.id = :notificacionId and n.empresaId = :empresaId and d.usuarioId = :usuarioId")
    Optional<NotificacionDestinatario> buscarDestinatario(@Param("empresaId") UUID empresaId, @Param("usuarioId") UUID usuarioId, @Param("notificacionId") UUID notificacionId);
    long countByUsuarioIdAndLeidaFalse(UUID usuarioId);
}
