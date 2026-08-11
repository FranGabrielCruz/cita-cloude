package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import com.citacloud.app.repositories.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> listarPorEmpresa(UUID empresaId) {
        return citaRepository.findByEmpresaId(empresaId);
    }

    public List<Cita> listarPorFecha(UUID empresaId, LocalDate fecha) {
        return citaRepository.findByEmpresaIdAndFecha(empresaId, fecha);
    }

    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    /**
     * Registra una cita verificando que todos los datos pertenecen a la empresa
     * autenticada y que el médico no tenga otro turno en el mismo intervalo.
     */
    public Cita registrar(UUID empresaId, Cita cita) {
        validarDatos(empresaId, cita);
        if (citaRepository.existsByEmpresaIdAndMedicoIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                empresaId, cita.getMedico().getId(), cita.getFecha(), cita.getHoraFin(), cita.getHoraInicio())) {
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario.");
        }

        cita.setEmpresaId(empresaId);
        cita.setEstado("PENDIENTE");
        return citaRepository.save(cita);
    }

    public Cita actualizar(UUID empresaId, Cita cita) {
        if (cita == null || cita.getId() == null || !citaRepository.existsById(cita.getId())) {
            throw new IllegalArgumentException("La cita no existe.");
        }
        validarDatos(empresaId, cita);
        boolean tieneConflicto = citaRepository.findByEmpresaId(empresaId).stream()
                .filter(existente -> !existente.getId().equals(cita.getId()))
                .anyMatch(existente -> existente.getMedico().getId().equals(cita.getMedico().getId())
                        && existente.getFecha().equals(cita.getFecha())
                        && existente.getHoraInicio().isBefore(cita.getHoraFin())
                        && existente.getHoraFin().isAfter(cita.getHoraInicio()));
        if (tieneConflicto) {
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario.");
        }
        return citaRepository.save(cita);
    }

    private void validarDatos(UUID empresaId, Cita cita) {
        if (empresaId == null || cita == null || cita.getPaciente() == null || cita.getMedico() == null
                || cita.getSucursal() == null || cita.getFecha() == null || cita.getHoraInicio() == null
                || cita.getHoraFin() == null) {
            throw new IllegalArgumentException("Completa los datos obligatorios de la cita.");
        }
        if (!empresaId.equals(cita.getPaciente().getEmpresaId())
                || !empresaId.equals(cita.getMedico().getEmpresaId())
                || !empresaId.equals(cita.getSucursal().getEmpresaId())
                || (cita.getConsultorio() != null && !empresaId.equals(cita.getConsultorio().getEmpresaId()))) {
            throw new IllegalArgumentException("No puedes asociar datos de otra empresa.");
        }
        if (!cita.getHoraFin().isAfter(cita.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de finalización debe ser posterior a la hora de inicio.");
        }
    }

    public void cambiarEstado(UUID citaId, String nuevoEstado) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.setEstado(nuevoEstado);
            citaRepository.save(cita);
        }
    }

    public void cancelar(UUID empresaId, UUID citaId) {
        Cita cita = citaRepository.findById(citaId)
                .filter(encontrada -> empresaId.equals(encontrada.getEmpresaId()))
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para esta empresa."));
        if ("CANCELADA".equals(cita.getEstado())) {
            throw new IllegalArgumentException("La cita ya está cancelada.");
        }
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }
}
