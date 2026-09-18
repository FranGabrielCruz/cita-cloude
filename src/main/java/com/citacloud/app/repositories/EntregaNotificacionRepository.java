package com.citacloud.app.repositories;
import com.citacloud.app.models.EntregaNotificacion; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface EntregaNotificacionRepository extends JpaRepository<EntregaNotificacion,UUID>{boolean existsByEmpresaIdAndEntidadIdAndEventoAndCanal(UUID empresaId,UUID entidadId,String evento,String canal); List<EntregaNotificacion> findByEmpresaIdAndEntidadIdOrderByCreadaEnDesc(UUID empresaId,UUID entidadId);}
