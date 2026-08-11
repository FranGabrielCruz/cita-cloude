package com.citacloud.app.repositories;

import com.citacloud.app.models.SeguroPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeguroPacienteRepository extends JpaRepository<SeguroPaciente, UUID> {
    Optional<SeguroPaciente> findFirstByEmpresaIdAndPacienteIdAndActivoTrue(UUID empresaId, UUID pacienteId);
    List<SeguroPaciente> findByEmpresaIdAndPacienteIdAndActivoTrue(UUID empresaId, UUID pacienteId);
}
