package com.citacloud.app.repositories; import com.citacloud.app.models.Pago; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PagoRepository extends JpaRepository<Pago,UUID>{ List<Pago> findByEmpresaIdOrderByFechaDesc(UUID empresaId); }
