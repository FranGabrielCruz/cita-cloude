package com.citacloud.app.repositories;
import com.citacloud.app.models.ReembolsoPago; import org.springframework.data.jpa.repository.*; import java.math.BigDecimal; import java.util.*;
public interface ReembolsoPagoRepository extends JpaRepository<ReembolsoPago,UUID>{ @Query("select coalesce(sum(r.monto),0) from ReembolsoPago r where r.empresaId=?1 and r.pago.id=?2") BigDecimal totalPorPago(UUID empresaId,UUID pagoId); }
