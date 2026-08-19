package com.citacloud.app.repositories;
import com.citacloud.app.models.Diagnostico; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, UUID> { java.util.List<Diagnostico> findByEmpresaIdAndPacienteId(UUID empresaId, UUID pacienteId); }
