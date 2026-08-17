package com.citacloud.app.repositories; import com.citacloud.app.models.RecordatorioCita; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RecordatorioCitaRepository extends JpaRepository<RecordatorioCita,UUID>{ List<RecordatorioCita> findByEmpresaIdOrderByFechaProgramadaDesc(UUID empresaId); }
