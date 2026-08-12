package com.citacloud.app.repositories;

import com.citacloud.app.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {
    List<Rol> findByEmpresaId(UUID empresaId);
    java.util.Optional<Rol> findByIdAndEmpresaId(UUID id, UUID empresaId);
    java.util.Optional<Rol> findByEmpresaIdAndNombre(UUID empresaId, String nombre);
}
