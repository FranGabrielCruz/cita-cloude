package com.citacloud.app.services;

import com.citacloud.app.models.Especialidad;
import com.citacloud.app.repositories.EspecialidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    public List<Especialidad> listarPorEmpresa(UUID empresaId) {
        return especialidadRepository.findByEmpresaId(empresaId);
    }

    public Especialidad guardar(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }
}
