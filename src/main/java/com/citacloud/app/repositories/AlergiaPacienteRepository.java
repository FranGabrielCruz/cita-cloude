package com.citacloud.app.repositories;
import com.citacloud.app.models.AlergiaPaciente; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AlergiaPacienteRepository extends JpaRepository<AlergiaPaciente, UUID> { java.util.List<AlergiaPaciente> findByEmpresaIdAndPacienteId(UUID empresaId, UUID pacienteId); }
