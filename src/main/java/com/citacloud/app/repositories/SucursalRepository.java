package com.citacloud.app.repositories;

import com.citacloud.app.models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, UUID> {
    List<Sucursal> findByEmpresaId(UUID empresaId);
    List<Sucursal> findByEmpresaIdAndActivaTrue(UUID empresaId);
    Optional<Sucursal> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
