package com.citacloud.app.services;

import com.citacloud.app.models.Aseguradora;
import com.citacloud.app.repositories.AseguradoraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AseguradoraService {

    private final AseguradoraRepository aseguradoraRepository;

    public AseguradoraService(AseguradoraRepository aseguradoraRepository) {
        this.aseguradoraRepository = aseguradoraRepository;
    }

    public List<Aseguradora> listarActivas(UUID empresaId) {
        return aseguradoraRepository.findByEmpresaIdAndActivaTrue(empresaId);
    }
}
