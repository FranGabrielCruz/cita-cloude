package com.citacloud.app.repositories;
import com.citacloud.app.models.CargoFinanciero; import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface CargoFinancieroRepository extends JpaRepository<CargoFinanciero,UUID> {
 List<CargoFinanciero> findByEmpresaIdAndPacienteIdAndSaldoGreaterThanOrderByFechaAsc(UUID empresaId, UUID pacienteId, java.math.BigDecimal saldo);
 @Query("select coalesce(sum(c.saldo),0) from CargoFinanciero c where c.empresaId=?1") java.math.BigDecimal saldoPendiente(UUID empresaId);
 Optional<CargoFinanciero> findByEmpresaIdAndFacturaId(UUID empresaId, UUID facturaId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from CargoFinanciero c where c.id=:id and c.empresaId=:empresaId") Optional<CargoFinanciero> bloquearPorIdYEmpresa(@Param("id") UUID id,@Param("empresaId") UUID empresaId);
}
