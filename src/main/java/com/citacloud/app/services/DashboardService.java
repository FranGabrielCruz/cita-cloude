package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import com.citacloud.app.repositories.CitaRepository;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.repositories.PacienteRepository;
import com.citacloud.app.security.AuthService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public DashboardService(CitaRepository citaRepository,
                            PacienteRepository pacienteRepository,
                            MedicoRepository medicoRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    public long getPendientesCount(UUID empresaId) {
        return citaRepository.countByEmpresaIdAndEstado(empresaId, "PENDIENTE");
    }

    public long getConfirmadasCount(UUID empresaId) {
        return citaRepository.countByEmpresaIdAndEstado(empresaId, "CONFIRMADA");
    }

    public long getAtendidasCount(UUID empresaId) {
        return citaRepository.countByEmpresaIdAndEstado(empresaId, "ATENDIDA");
    }

    public long getCitasHoyPorEstado(UUID empresaId, String estado) {
        return contarPorEstado(citaRepository.findByEmpresaIdAndFecha(empresaId, LocalDate.now()), estado);
    }

    public long getMedicosActivosCount(UUID empresaId) {
        return medicoRepository.countByEmpresaIdAndActivoTrue(empresaId);
    }

    public ResumenEstadosHoy getResumenEstadosHoy(UUID empresaId) {
        List<Cita> citasHoy = citaRepository.findByEmpresaIdAndFecha(empresaId, LocalDate.now());
        long confirmadas = contarPorEstado(citasHoy, "CONFIRMADA");
        long pendientes = contarPorEstado(citasHoy, "PENDIENTE");
        long canceladas = contarPorEstado(citasHoy, "CANCELADA");
        return new ResumenEstadosHoy(citasHoy.size(), confirmadas, pendientes, canceladas);
    }

    private long contarPorEstado(List<Cita> citas, String estado) {
        return citas.stream().filter(cita -> estado.equals(cita.getEstado())).count();
    }

    public List<Cita> getCitasRecientes(UUID empresaId) {
        return citaRepository.findRecientesByEmpresaId(empresaId);
    }

    public List<Cita> getAgendaHoy(UUID empresaId) {
        return citaRepository.findByEmpresaIdAndFecha(empresaId, LocalDate.now()).stream()
                .sorted(Comparator.comparing(Cita::getHoraInicio))
                .toList();
    }

    public record ResumenEstadosHoy(long total, long confirmadas, long pendientes, long canceladas) {}
}
