package com.citacloud.app.services;

import com.citacloud.app.models.HorarioMedico;
import com.citacloud.app.repositories.HorarioMedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class HorarioMedicoService {
    private final HorarioMedicoRepository horarioRepository;
    public HorarioMedicoService(HorarioMedicoRepository horarioRepository) { this.horarioRepository = horarioRepository; }
    public List<HorarioMedico> listar(UUID empresaId) { return horarioRepository.findByEmpresaId(empresaId); }

    @Transactional
    public List<HorarioMedico> crearBloques(UUID empresaId, HorarioMedico base, Set<Integer> dias) {
        validarBase(empresaId, base, dias);
        StringBuilder conflictos = new StringBuilder();
        for (Integer dia : dias) {
            for (HorarioMedico existente : horarioRepository.findByEmpresaIdAndMedicoIdAndDiaSemana(empresaId, base.getMedico().getId(), dia)) {
                if (Boolean.TRUE.equals(existente.getActivo()) && seSuperpone(base, existente)) {
                    if (!conflictos.isEmpty()) conflictos.append(", ");
                    conflictos.append(nombreDia(dia)).append(" ").append(existente.getHoraInicio()).append("-").append(existente.getHoraFin());
                }
            }
        }
        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede guardar el horario. El m\u00e9dico ya tiene un horario configurado: " + conflictos + ".");
        }
        return dias.stream().sorted().map(dia -> {
            HorarioMedico bloque = new HorarioMedico();
            bloque.setEmpresaId(empresaId); bloque.setMedico(base.getMedico()); bloque.setSucursal(base.getSucursal());
            bloque.setConsultorio(base.getConsultorio()); bloque.setDiaSemana(dia); bloque.setHoraInicio(base.getHoraInicio());
            bloque.setHoraFin(base.getHoraFin()); bloque.setDuracionCitaMinutos(base.getDuracionCitaMinutos()); bloque.setActivo(base.getActivo());
            return horarioRepository.save(bloque);
        }).toList();
    }

    public void cambiarEstado(UUID empresaId, UUID horarioId, boolean activo) {
        HorarioMedico horario = horarioRepository.findByIdAndEmpresaId(horarioId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado para esta empresa."));
        horario.setActivo(activo); horarioRepository.save(horario);
    }

    private void validarBase(UUID empresaId, HorarioMedico base, Set<Integer> dias) {
        if (empresaId == null || base == null || base.getMedico() == null || base.getSucursal() == null || dias == null || dias.isEmpty()
                || base.getHoraInicio() == null || base.getHoraFin() == null || base.getDuracionCitaMinutos() == null) {
            throw new IllegalArgumentException("Completa los campos obligatorios del horario.");
        }
        if (!base.getHoraFin().isAfter(base.getHoraInicio())) throw new IllegalArgumentException("La hora fin debe ser posterior a la hora inicio.");
        if (base.getDuracionCitaMinutos() <= 0 || base.getDuracionCitaMinutos() > 240) throw new IllegalArgumentException("La duraci\u00f3n de cita debe ser v\u00e1lida.");
        if (!empresaId.equals(base.getMedico().getEmpresaId()) || !empresaId.equals(base.getSucursal().getEmpresaId())
                || (base.getConsultorio() != null && !empresaId.equals(base.getConsultorio().getEmpresaId()))) {
            throw new IllegalArgumentException("El m\u00e9dico, sucursal y consultorio deben pertenecer a esta empresa.");
        }
        if (dias.stream().anyMatch(dia -> dia < 1 || dia > 7)) throw new IllegalArgumentException("Selecciona d\u00edas v\u00e1lidos.");
    }
    private boolean seSuperpone(HorarioMedico nuevo, HorarioMedico existente) {
        return nuevo.getHoraInicio().isBefore(existente.getHoraFin()) && nuevo.getHoraFin().isAfter(existente.getHoraInicio());
    }
    private String nombreDia(int dia) { return new String[]{"Lunes", "Martes", "Mi\u00e9rcoles", "Jueves", "Viernes", "S\u00e1bado", "Domingo"}[dia - 1]; }
}
