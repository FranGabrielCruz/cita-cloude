package com.citacloud.app.repositories;

import com.citacloud.app.models.MuestraLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MuestraLaboratorioRepository extends JpaRepository<MuestraLaboratorio, UUID> {
    List<MuestraLaboratorio> findByEmpresaIdAndOrdenIdOrderByTomadaEnDesc(UUID empresaId, UUID ordenId);
    Optional<MuestraLaboratorio> findByIdAndEmpresaId(UUID id, UUID empresaId);
    long countByEmpresaIdAndEstado(UUID empresaId, String estado);
    boolean existsByEmpresaIdAndCodigo(UUID empresaId, String codigo);
}
