package com.citacloud.app.services;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.repositories.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Empresa buscar(UUID empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontr\u00f3 la instituci\u00f3n."));
    }

    public Empresa guardar(UUID empresaId, String nombre, String rnc, String telefono, String email, String direccion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la instituci\u00f3n es obligatorio.");
        }
        Empresa empresa = buscar(empresaId);
        empresa.setNombre(nombre.trim());
        empresa.setRncIdentificacion(limpiar(rnc));
        empresa.setTelefono(limpiar(telefono));
        empresa.setEmail(limpiar(email));
        empresa.setDireccion(limpiar(direccion));
        empresa.setActualizadoEn(LocalDateTime.now());
        return empresaRepository.save(empresa);
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
