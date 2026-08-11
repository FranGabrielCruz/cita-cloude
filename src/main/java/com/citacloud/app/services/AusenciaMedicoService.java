package com.citacloud.app.services;

import com.citacloud.app.models.AusenciaMedico;
import com.citacloud.app.repositories.AusenciaMedicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class AusenciaMedicoService {
    private final AusenciaMedicoRepository ausenciaRepository;
    public AusenciaMedicoService(AusenciaMedicoRepository ausenciaRepository) { this.ausenciaRepository = ausenciaRepository; }
    public List<AusenciaMedico> listar(UUID empresaId) { return ausenciaRepository.findByEmpresaId(empresaId); }
    public List<AusenciaMedico> listar(UUID empresaId, UUID medicoId) { return ausenciaRepository.findByEmpresaIdAndMedicoIdAndActivoTrue(empresaId, medicoId); }
    public AusenciaMedico guardar(UUID empresaId, AusenciaMedico ausencia) {
        if (ausencia == null || ausencia.getMedico() == null || ausencia.getFechaInicio() == null || ausencia.getFechaFin() == null || !ausencia.getFechaFin().isAfter(ausencia.getFechaInicio())) throw new IllegalArgumentException("La ausencia debe tener un intervalo de fechas v\u00e1lido.");
        if (!empresaId.equals(ausencia.getMedico().getEmpresaId())) throw new IllegalArgumentException("El m\u00e9dico no pertenece a esta empresa.");
        ausencia.setEmpresaId(empresaId); ausencia.setActivo(true); return ausenciaRepository.save(ausencia);
    }

    public AusenciaMedico actualizar(UUID empresaId, UUID ausenciaId, AusenciaMedico datos) {
        AusenciaMedico ausencia = ausenciaRepository.findByIdAndEmpresaId(ausenciaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Ausencia no encontrada para esta empresa."));
        validar(empresaId, datos);
        ausencia.setMedico(datos.getMedico()); ausencia.setFechaInicio(datos.getFechaInicio()); ausencia.setFechaFin(datos.getFechaFin()); ausencia.setMotivo(datos.getMotivo());
        return ausenciaRepository.save(ausencia);
    }

    public void cancelar(UUID empresaId, UUID ausenciaId) {
        AusenciaMedico ausencia = ausenciaRepository.findByIdAndEmpresaId(ausenciaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Ausencia no encontrada para esta empresa."));
        ausencia.setActivo(false); ausenciaRepository.save(ausencia);
    }

    private void validar(UUID empresaId, AusenciaMedico ausencia) {
        if (ausencia == null || ausencia.getMedico() == null || ausencia.getFechaInicio() == null || ausencia.getFechaFin() == null || !ausencia.getFechaFin().isAfter(ausencia.getFechaInicio())) throw new IllegalArgumentException("La ausencia debe tener un intervalo de fechas v\u00e1lido.");
        if (!empresaId.equals(ausencia.getMedico().getEmpresaId())) throw new IllegalArgumentException("El m\u00e9dico no pertenece a esta empresa.");
    }
}
