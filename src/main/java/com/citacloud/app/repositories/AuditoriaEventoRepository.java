package com.citacloud.app.repositories;
import com.citacloud.app.models.AuditoriaEvento; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.LocalDateTime; import java.util.*;
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento,UUID>{
 List<AuditoriaEvento> findByEmpresaIdOrderByFechaDesc(UUID empresaId); List<AuditoriaEvento> findByEmpresaIdAndFechaBetweenOrderByFechaDesc(UUID empresaId, LocalDateTime desde, LocalDateTime hasta); Optional<AuditoriaEvento> findByIdAndEmpresaId(UUID id,UUID empresaId);
 @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId=:empresa AND a.fecha BETWEEN :desde AND :hasta "
   + "AND (:usuario IS NULL OR a.usuarioId=:usuario) AND (:modulo IS NULL OR a.modulo=:modulo) AND (:resultado IS NULL OR a.resultado=:resultado) "
   + "AND (:texto = '' OR lower(coalesce(a.modulo,'')) LIKE lower(concat('%',:texto,'%')) OR lower(coalesce(a.accion,'')) LIKE lower(concat('%',:texto,'%')) "
   + "OR lower(coalesce(a.recurso,'')) LIKE lower(concat('%',:texto,'%')) OR lower(coalesce(a.entidad,'')) LIKE lower(concat('%',:texto,'%'))) ORDER BY a.fecha DESC")
 Page<AuditoriaEvento> buscar(@Param("empresa") UUID empresa,@Param("desde") LocalDateTime desde,@Param("hasta") LocalDateTime hasta,@Param("usuario") UUID usuario,@Param("modulo") String modulo,@Param("resultado") String resultado,@Param("texto") String texto, Pageable pageable);
}
