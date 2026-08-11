package com.citacloud.app.repositories;

import com.citacloud.app.models.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {
    List<Medico> findByEmpresaId(UUID empresaId);
    List<Medico> findByEmpresaIdAndActivoTrue(UUID empresaId);
    long countByEmpresaIdAndActivoTrue(UUID empresaId);
}
