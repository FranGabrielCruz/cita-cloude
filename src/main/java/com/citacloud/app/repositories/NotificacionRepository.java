package com.citacloud.app.repositories;

import com.citacloud.app.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {}
