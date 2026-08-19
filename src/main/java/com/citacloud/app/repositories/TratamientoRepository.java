package com.citacloud.app.repositories;
import com.citacloud.app.models.Tratamiento; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TratamientoRepository extends JpaRepository<Tratamiento, UUID> { java.util.List<Tratamiento> findByEmpresaIdAndPacienteId(UUID empresaId, UUID pacienteId); }
