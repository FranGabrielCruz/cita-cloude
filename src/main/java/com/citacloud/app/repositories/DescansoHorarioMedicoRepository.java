package com.citacloud.app.repositories;
import com.citacloud.app.models.DescansoHorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface DescansoHorarioMedicoRepository extends JpaRepository<DescansoHorarioMedico, UUID> {
    List<DescansoHorarioMedico> findByEmpresaIdAndHorarioId(UUID empresaId, UUID horarioId);
}
