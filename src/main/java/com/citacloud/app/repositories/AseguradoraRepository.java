package com.citacloud.app.repositories;

import com.citacloud.app.models.Aseguradora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AseguradoraRepository extends JpaRepository<Aseguradora, UUID> {
    List<Aseguradora> findByEmpresaId(UUID empresaId);
    List<Aseguradora> findByEmpresaIdAndActivaTrue(UUID empresaId);
    Optional<Aseguradora> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
