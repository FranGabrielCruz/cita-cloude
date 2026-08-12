package com.citacloud.app.repositories;

import com.citacloud.app.models.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {
    List<Permiso> findAllByOrderByNombreAsc();
}
