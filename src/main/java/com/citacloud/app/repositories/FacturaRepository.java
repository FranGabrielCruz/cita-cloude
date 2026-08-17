package com.citacloud.app.repositories; import com.citacloud.app.models.Factura; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface FacturaRepository extends JpaRepository<Factura,UUID>{ List<Factura> findByEmpresaIdOrderByFechaDesc(UUID empresaId); Optional<Factura> findByIdAndEmpresaId(UUID id,UUID empresaId); }
