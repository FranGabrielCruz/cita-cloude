package com.citacloud.app.repositories;
import com.citacloud.app.models.ConfiguracionNotificacionCita; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ConfiguracionNotificacionCitaRepository extends JpaRepository<ConfiguracionNotificacionCita,UUID>{Optional<ConfiguracionNotificacionCita> findByEmpresaIdAndEvento(UUID empresaId,String evento);}
