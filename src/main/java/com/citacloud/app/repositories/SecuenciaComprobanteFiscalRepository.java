package com.citacloud.app.repositories;

import com.citacloud.app.models.SecuenciaComprobanteFiscal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface SecuenciaComprobanteFiscalRepository extends JpaRepository<SecuenciaComprobanteFiscal, UUID> {
    List<SecuenciaComprobanteFiscal> findByEmpresaIdOrderByNombre(UUID empresaId);
    List<SecuenciaComprobanteFiscal> findByEmpresaIdAndActivaTrueOrderByNombre(UUID empresaId);
    Optional<SecuenciaComprobanteFiscal> findByIdAndEmpresaId(UUID id, UUID empresaId);
    Optional<SecuenciaComprobanteFiscal> findByEmpresaIdAndTipoAndActivaTrue(UUID empresaId, String tipo);
    boolean existsByEmpresaIdAndTipoAndIdNot(UUID empresaId, String tipo, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SecuenciaComprobanteFiscal s where s.id=:id and s.empresaId=:empresaId")
    Optional<SecuenciaComprobanteFiscal> bloquear(@Param("id") UUID id, @Param("empresaId") UUID empresaId);
}
