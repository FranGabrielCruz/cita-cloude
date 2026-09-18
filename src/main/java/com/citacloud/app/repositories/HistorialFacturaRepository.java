package com.citacloud.app.repositories;

import com.citacloud.app.models.HistorialFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HistorialFacturaRepository extends JpaRepository<HistorialFactura, UUID> {
    List<HistorialFactura> findByEmpresaIdAndFacturaIdOrderByCreadoEnDesc(UUID empresaId, UUID facturaId);
}
