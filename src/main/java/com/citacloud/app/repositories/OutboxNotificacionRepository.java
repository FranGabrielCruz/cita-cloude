package com.citacloud.app.repositories;

import com.citacloud.app.models.OutboxNotificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxNotificacionRepository extends JpaRepository<OutboxNotificacion, UUID> {
    List<OutboxNotificacion> findByEstadoAndProximoIntentoLessThanEqualOrderByCreadoEnAsc(
            String estado, LocalDateTime fecha, Pageable pageable);
    long countByEstado(String estado);
    Optional<OutboxNotificacion> findFirstByEstadoOrderByCreadoEnAsc(String estado);
}
