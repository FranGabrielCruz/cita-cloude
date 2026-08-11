package com.citacloud.app.services;

import com.citacloud.app.models.Consultorio;
import com.citacloud.app.repositories.ConsultorioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultorioService {

    private final ConsultorioRepository consultorioRepository;

    public ConsultorioService(ConsultorioRepository consultorioRepository) {
        this.consultorioRepository = consultorioRepository;
    }

    public List<Consultorio> listarPorEmpresa(UUID empresaId) {
        return consultorioRepository.findByEmpresaId(empresaId);
    }

    public Consultorio guardar(Consultorio consultorio) {
        return consultorioRepository.save(consultorio);
    }
}
