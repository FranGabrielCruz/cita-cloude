package com.citacloud.app.services;

import com.citacloud.app.models.Medico;
import com.citacloud.app.repositories.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    public List<Medico> listarPorEmpresa(UUID empresaId) {
        return medicoRepository.findByEmpresaId(empresaId);
    }

    public List<Medico> listarActivos(UUID empresaId) {
        return medicoRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    public Medico guardar(Medico medico) {
        return medicoRepository.save(medico);
    }
}
