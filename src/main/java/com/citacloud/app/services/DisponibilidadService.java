package com.citacloud.app.services;

import com.citacloud.app.models.AusenciaMedico;
import com.citacloud.app.models.DescansoHorarioMedico;
import com.citacloud.app.models.HorarioMedico;
import com.citacloud.app.repositories.AusenciaMedicoRepository;
import com.citacloud.app.repositories.CitaRepository;
import com.citacloud.app.repositories.DescansoHorarioMedicoRepository;
import com.citacloud.app.repositories.HorarioMedicoRepository;
import com.citacloud.app.repositories.MedicoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class DisponibilidadService {
    private final MedicoRepository medicoRepository; private final HorarioMedicoRepository horarioRepository;
    private final DescansoHorarioMedicoRepository descansoRepository; private final AusenciaMedicoRepository ausenciaRepository;
    private final CitaRepository citaRepository;
    public DisponibilidadService(MedicoRepository medicoRepository, HorarioMedicoRepository horarioRepository, DescansoHorarioMedicoRepository descansoRepository, AusenciaMedicoRepository ausenciaRepository, CitaRepository citaRepository) {
        this.medicoRepository = medicoRepository; this.horarioRepository = horarioRepository; this.descansoRepository = descansoRepository; this.ausenciaRepository = ausenciaRepository; this.citaRepository = citaRepository;
    }
    public List<LocalTime> obtenerSlotsDisponibles(UUID empresaId, UUID medicoId, LocalDate fecha) {
        return obtenerSlotsDisponibles(empresaId, medicoId, fecha, null);
    }
    public List<LocalTime> obtenerSlotsDisponibles(UUID empresaId, UUID medicoId, LocalDate fecha, UUID citaExcluida) {
        medicoRepository.findByIdAndEmpresaId(medicoId, empresaId).orElseThrow(() -> new IllegalArgumentException("M\u00e9dico no encontrado para esta empresa."));
        int dia = fecha.getDayOfWeek().getValue();
        List<LocalTime> disponibles = new ArrayList<>();
        for (HorarioMedico horario : horarioRepository.findByEmpresaIdAndMedicoIdAndDiaSemanaAndActivoTrue(empresaId, medicoId, dia)) {
            List<DescansoHorarioMedico> descansos = descansoRepository.findByEmpresaIdAndHorarioId(empresaId, horario.getId());
            LocalTime slot = horario.getHoraInicio();
            while (!slot.plusMinutes(horario.getDuracionCitaMinutos()).isAfter(horario.getHoraFin())) {
                LocalTime finSlot = slot.plusMinutes(horario.getDuracionCitaMinutos());
                boolean horarioPasado = fecha.equals(LocalDate.now()) && !slot.isAfter(LocalTime.now());
                if (!horarioPasado && !enDescanso(descansos, slot, finSlot) && !enAusencia(empresaId, medicoId, fecha, slot, finSlot)
                        && !ocupado(empresaId, medicoId, fecha, slot, finSlot, citaExcluida)) disponibles.add(slot);
                slot = finSlot;
            }
        }
        return disponibles;
    }
    public boolean estaDisponible(UUID empresaId, UUID medicoId, LocalDate fecha, LocalTime inicio, LocalTime fin, UUID citaExcluida) {
        int dia = fecha.getDayOfWeek().getValue();
        for (HorarioMedico horario : horarioRepository.findByEmpresaIdAndMedicoIdAndDiaSemanaAndActivoTrue(empresaId, medicoId, dia)) {
            LocalTime finEsperado = inicio.plusMinutes(horario.getDuracionCitaMinutos());
            if (inicio.isBefore(horario.getHoraInicio()) || !finEsperado.equals(fin) || finEsperado.isAfter(horario.getHoraFin())) continue;
            List<DescansoHorarioMedico> descansos = descansoRepository.findByEmpresaIdAndHorarioId(empresaId, horario.getId());
            if (!enDescanso(descansos, inicio, fin) && !enAusencia(empresaId, medicoId, fecha, inicio, fin)
                    && !ocupado(empresaId, medicoId, fecha, inicio, fin, citaExcluida)) return true;
        }
        return false;
    }
    public Optional<LocalTime> obtenerHoraFinSugerida(UUID empresaId, UUID medicoId, LocalDate fecha, LocalTime inicio) {
        int dia = fecha.getDayOfWeek().getValue();
        return horarioRepository.findByEmpresaIdAndMedicoIdAndDiaSemanaAndActivoTrue(empresaId, medicoId, dia).stream()
                .filter(horario -> !inicio.isBefore(horario.getHoraInicio()))
                .map(horario -> new java.util.AbstractMap.SimpleEntry<>(horario, inicio.plusMinutes(horario.getDuracionCitaMinutos())))
                .filter(par -> !par.getValue().isAfter(par.getKey().getHoraFin()))
                .filter(par -> java.time.Duration.between(par.getKey().getHoraInicio(), inicio).toMinutes() % par.getKey().getDuracionCitaMinutos() == 0)
                .map(java.util.Map.Entry::getValue).findFirst();
    }
    private boolean enDescanso(List<DescansoHorarioMedico> descansos, LocalTime inicio, LocalTime fin) { return descansos.stream().anyMatch(d -> inicio.isBefore(d.getHoraFin()) && fin.isAfter(d.getHoraInicio())); }
    private boolean enAusencia(UUID empresaId, UUID medicoId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        LocalDateTime desde = LocalDateTime.of(fecha, inicio), hasta = LocalDateTime.of(fecha, fin);
        return ausenciaRepository.findByEmpresaIdAndMedicoIdAndActivoTrue(empresaId, medicoId).stream().anyMatch(a -> desde.isBefore(a.getFechaFin()) && hasta.isAfter(a.getFechaInicio()));
    }
    private boolean ocupado(UUID empresaId, UUID medicoId, LocalDate fecha, LocalTime inicio, LocalTime fin, UUID citaExcluida) {
        return citaRepository.findByEmpresaIdAndFecha(empresaId, fecha).stream().anyMatch(c -> !"CANCELADA".equals(c.getEstado()) && (citaExcluida == null || !citaExcluida.equals(c.getId())) && c.getMedico().getId().equals(medicoId) && inicio.isBefore(c.getHoraFin()) && fin.isAfter(c.getHoraInicio()));
    }
}
