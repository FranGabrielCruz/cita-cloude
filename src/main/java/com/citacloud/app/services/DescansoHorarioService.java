package com.citacloud.app.services;

import com.citacloud.app.models.DescansoHorarioMedico;
import com.citacloud.app.models.HorarioMedico;
import com.citacloud.app.repositories.DescansoHorarioMedicoRepository;
import com.citacloud.app.repositories.HorarioMedicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class DescansoHorarioService {
    private final DescansoHorarioMedicoRepository descansoRepository; private final HorarioMedicoRepository horarioRepository;
    public DescansoHorarioService(DescansoHorarioMedicoRepository descansoRepository, HorarioMedicoRepository horarioRepository) { this.descansoRepository = descansoRepository; this.horarioRepository = horarioRepository; }
    public List<DescansoHorarioMedico> listar(UUID empresaId, UUID horarioId) { return descansoRepository.findByEmpresaIdAndHorarioId(empresaId, horarioId); }
    public DescansoHorarioMedico guardar(UUID empresaId, DescansoHorarioMedico descanso) {
        HorarioMedico horario = horarioRepository.findByIdAndEmpresaId(descanso.getHorarioId(), empresaId).orElseThrow(() -> new IllegalArgumentException("Horario no encontrado."));
        if (descanso.getHoraInicio() == null || descanso.getHoraFin() == null || !descanso.getHoraFin().isAfter(descanso.getHoraInicio())) throw new IllegalArgumentException("La hora fin del descanso debe ser posterior a la hora inicio.");
        if (descanso.getHoraInicio().isBefore(horario.getHoraInicio()) || descanso.getHoraFin().isAfter(horario.getHoraFin())) throw new IllegalArgumentException("El descanso debe estar contenido dentro del horario laboral.");
        descanso.setEmpresaId(empresaId); return descansoRepository.save(descanso);
    }
}
