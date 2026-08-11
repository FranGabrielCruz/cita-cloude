package com.citacloud.app.repositories;

import com.citacloud.app.models.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, UUID> {
    List<Consultorio> findByEmpresaId(UUID empresaId);
    List<Consultorio> findByEmpresaIdAndActivoTrue(UUID empresaId);
    List<Consultorio> findByEmpresaIdAndSucursalId(UUID empresaId, UUID sucursalId);
}
