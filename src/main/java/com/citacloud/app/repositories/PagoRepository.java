package com.citacloud.app.repositories; import com.citacloud.app.models.Pago; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface PagoRepository extends JpaRepository<Pago,UUID>{ List<Pago> findByEmpresaIdOrderByFechaDesc(UUID empresaId); List<Pago> findByEmpresaIdOrderByCreadoEnDesc(UUID empresaId); Optional<Pago> findByIdAndEmpresaId(UUID id,UUID empresaId); Optional<Pago> findByEmpresaIdAndClaveIdempotencia(UUID empresaId,String claveIdempotencia);
 @Query("select distinct p from Pago p where p.empresaId=?1 and (p.factura.id=?2 or p.id in (select a.pago.id from PagoAplicacion a where a.empresaId=?1 and a.cargo.factura.id=?2)) order by p.creadoEn desc") List<Pago> findByEmpresaIdAndFactura(UUID empresaId,UUID facturaId);
}
