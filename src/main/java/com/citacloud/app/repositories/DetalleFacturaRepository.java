package com.citacloud.app.repositories;

import com.citacloud.app.models.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, UUID> {
    List<DetalleFactura> findByEmpresaId(UUID empresaId);
    List<DetalleFactura> findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(UUID empresaId, UUID facturaId);
    void deleteByEmpresaIdAndFacturaId(UUID empresaId, UUID facturaId);
}
