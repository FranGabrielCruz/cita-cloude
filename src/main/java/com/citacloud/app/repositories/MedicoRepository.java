package com.citacloud.app.repositories;

import com.citacloud.app.models.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {
    List<Medico> findByEmpresaId(UUID empresaId);
    List<Medico> findByEmpresaIdAndActivoTrue(UUID empresaId);
    boolean existsByEmpresaIdAndCodigo(UUID empresaId, String codigo);
    Optional<Medico> findByIdAndEmpresaId(UUID id, UUID empresaId);
    Optional<Medico> findByEmpresaIdAndUsuarioId(UUID empresaId, UUID usuarioId);
    long countByEmpresaIdAndActivoTrue(UUID empresaId);
}
