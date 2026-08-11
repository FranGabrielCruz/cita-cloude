package com.citacloud.app.repositories;

import com.citacloud.app.models.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {
    List<Especialidad> findByEmpresaId(UUID empresaId);
    List<Especialidad> findByEmpresaIdAndActivaTrue(UUID empresaId);
}
