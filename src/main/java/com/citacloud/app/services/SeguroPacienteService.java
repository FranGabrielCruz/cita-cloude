package com.citacloud.app.services;

import com.citacloud.app.models.Aseguradora;
import com.citacloud.app.models.SeguroPaciente;
import com.citacloud.app.repositories.AseguradoraRepository;
import com.citacloud.app.repositories.SeguroPacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeguroPacienteService {

    private final SeguroPacienteRepository seguroPacienteRepository;
    private final AseguradoraRepository aseguradoraRepository;

    public SeguroPacienteService(SeguroPacienteRepository seguroPacienteRepository,
                                 AseguradoraRepository aseguradoraRepository) {
        this.seguroPacienteRepository = seguroPacienteRepository;
        this.aseguradoraRepository = aseguradoraRepository;
    }

    public Optional<SeguroPaciente> obtenerActivo(UUID empresaId, UUID pacienteId) {
        return seguroPacienteRepository.findFirstByEmpresaIdAndPacienteIdAndActivoTrue(empresaId, pacienteId);
    }

    public String nombreSeguroActivo(UUID empresaId, UUID pacienteId) {
        return obtenerActivo(empresaId, pacienteId)
                .flatMap(seguro -> aseguradoraRepository.findById(seguro.getAseguradoraId()))
                .map(Aseguradora::getNombre)
                .orElse("-");
    }

    public Optional<Aseguradora> aseguradoraDelSeguroActivo(UUID empresaId, UUID pacienteId) {
        return obtenerActivo(empresaId, pacienteId)
                .flatMap(seguro -> aseguradoraRepository.findById(seguro.getAseguradoraId()))
                .filter(aseguradora -> empresaId.equals(aseguradora.getEmpresaId())
                        && Boolean.TRUE.equals(aseguradora.getActiva()));
    }

    public void actualizarSeguro(UUID empresaId, UUID pacienteId, Aseguradora aseguradora, String numeroPoliza) {
        List<SeguroPaciente> segurosActuales = seguroPacienteRepository
                .findByEmpresaIdAndPacienteIdAndActivoTrue(empresaId, pacienteId);
        segurosActuales.forEach(seguro -> seguro.setActivo(false));
        seguroPacienteRepository.saveAll(segurosActuales);

        if (aseguradora == null) {
            return;
        }
        if (!empresaId.equals(aseguradora.getEmpresaId()) || !Boolean.TRUE.equals(aseguradora.getActiva())) {
            throw new IllegalArgumentException("El seguro seleccionado no estÃ¡ disponible.");
        }
        if (numeroPoliza == null || numeroPoliza.isBlank()) {
            throw new IllegalArgumentException("Ingresa el nÃºmero de pÃ³liza del seguro.");
        }

        SeguroPaciente seguro = new SeguroPaciente();
        seguro.setEmpresaId(empresaId);
        seguro.setPacienteId(pacienteId);
        seguro.setAseguradoraId(aseguradora.getId());
        seguro.setNumeroPoliza(numeroPoliza.trim());
        seguro.setActivo(true);
        seguroPacienteRepository.save(seguro);
    }
}
