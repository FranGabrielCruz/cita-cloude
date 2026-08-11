package com.citacloud.app.repositories;
import com.citacloud.app.models.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, UUID> {
    List<HorarioMedico> findByEmpresaId(UUID empresaId);
    List<HorarioMedico> findByEmpresaIdAndMedicoIdAndDiaSemana(UUID empresaId, UUID medicoId, Integer diaSemana);
    List<HorarioMedico> findByEmpresaIdAndMedicoIdAndDiaSemanaAndActivoTrue(UUID empresaId, UUID medicoId, Integer diaSemana);
    Optional<HorarioMedico> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
