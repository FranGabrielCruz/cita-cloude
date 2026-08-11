package com.citacloud.app.repositories;
import com.citacloud.app.models.AusenciaMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface AusenciaMedicoRepository extends JpaRepository<AusenciaMedico, UUID> {
    List<AusenciaMedico> findByEmpresaId(UUID empresaId);
    List<AusenciaMedico> findByEmpresaIdAndMedicoId(UUID empresaId, UUID medicoId);
    List<AusenciaMedico> findByEmpresaIdAndMedicoIdAndActivoTrue(UUID empresaId, UUID medicoId);
    Optional<AusenciaMedico> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
