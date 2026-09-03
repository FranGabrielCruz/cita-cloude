package com.citacloud.app.repositories;

import com.citacloud.app.models.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface FacturaRepository extends JpaRepository<Factura, UUID>, JpaSpecificationExecutor<Factura> {
    List<Factura> findByEmpresaIdOrderByFechaDesc(UUID empresaId);
    Optional<Factura> findByIdAndEmpresaId(UUID id, UUID empresaId);
    Optional<Factura> findByEmpresaIdAndClaveIdempotencia(UUID empresaId, String claveIdempotencia);
    Optional<Factura> findByEmpresaIdAndOrigenTipoAndOrigenId(UUID empresaId, String origenTipo, UUID origenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id=:id and f.empresaId=:empresaId")
    Optional<Factura> bloquearPorIdYEmpresa(@Param("id") UUID id, @Param("empresaId") UUID empresaId);
}
