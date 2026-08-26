package com.citacloud.app.repositories;
import com.citacloud.app.models.PagoAplicacion; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PagoAplicacionRepository extends JpaRepository<PagoAplicacion,UUID>{ List<PagoAplicacion> findByEmpresaIdAndPagoId(UUID empresaId,UUID pagoId); }
