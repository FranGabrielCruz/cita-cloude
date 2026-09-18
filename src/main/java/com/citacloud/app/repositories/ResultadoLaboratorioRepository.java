package com.citacloud.app.repositories;

import com.citacloud.app.models.ResultadoLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ResultadoLaboratorioRepository extends JpaRepository<ResultadoLaboratorio, UUID> {
    List<ResultadoLaboratorio> findByEmpresaIdAndOrdenIdOrderByCreadoEnDesc(UUID empresaId, UUID ordenId);
    List<ResultadoLaboratorio> findByEmpresaIdAndEstadoOrderByCreadoEnDesc(UUID empresaId, String estado);
    Optional<ResultadoLaboratorio> findFirstByEmpresaIdAndDetalleOrdenIdOrderByVersionDesc(UUID empresaId, UUID detalleOrdenId);
    Optional<ResultadoLaboratorio> findByIdAndEmpresaId(UUID id, UUID empresaId);
    long countByEmpresaIdAndEstado(UUID empresaId, String estado);
}
