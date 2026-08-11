package com.citacloud.app.services;

import com.citacloud.app.models.Sucursal;
import com.citacloud.app.repositories.SucursalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalService(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    public List<Sucursal> listarPorEmpresa(UUID empresaId) {
        return sucursalRepository.findByEmpresaId(empresaId);
    }

    public Sucursal guardar(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }
}
