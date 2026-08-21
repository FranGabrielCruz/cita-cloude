package com.citacloud.app.repositories;

import com.citacloud.app.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    List<Paciente> findByEmpresaId(UUID empresaId);
    List<Paciente> findByEmpresaIdAndActivoTrue(UUID empresaId);
    Optional<Paciente> findByIdAndEmpresaId(UUID id, UUID empresaId);
    boolean existsByEmpresaIdAndDocumento(UUID empresaId, String documento);
    boolean existsByEmpresaIdAndNumeroExpediente(UUID empresaId, String numeroExpediente);
    long countByEmpresaId(UUID empresaId);

    @Query("SELECT p FROM Paciente p WHERE p.empresaId = :empresaId AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.documento) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Paciente> buscarPorTermino(@Param("empresaId") UUID empresaId, @Param("term") String term);
}
