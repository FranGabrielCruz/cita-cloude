package com.citacloud.app.repositories;

import com.citacloud.app.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CitaRepository extends JpaRepository<Cita, UUID> {
    List<Cita> findByEmpresaId(UUID empresaId);
    List<Cita> findByEmpresaIdAndFecha(UUID empresaId, LocalDate fecha);
    List<Cita> findByEmpresaIdAndFechaBetween(UUID empresaId, LocalDate inicio, LocalDate fin);
    boolean existsByEmpresaIdAndMedicoIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
            UUID empresaId, UUID medicoId, LocalDate fecha, LocalTime horaFin, LocalTime horaInicio);
    
    long countByEmpresaIdAndEstado(UUID empresaId, String estado);
    long countByEmpresaIdAndFecha(UUID empresaId, LocalDate fecha);

    @Query("SELECT c FROM Cita c WHERE c.empresaId = :empresaId ORDER BY c.fecha DESC, c.horaInicio DESC")
    List<Cita> findRecientesByEmpresaId(@Param("empresaId") UUID empresaId);
}
