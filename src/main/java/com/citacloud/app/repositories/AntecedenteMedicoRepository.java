package com.citacloud.app.repositories;
import com.citacloud.app.models.AntecedenteMedico; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AntecedenteMedicoRepository extends JpaRepository<AntecedenteMedico, UUID> { java.util.List<AntecedenteMedico> findByEmpresaIdAndPacienteId(UUID empresaId, UUID pacienteId); }
