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

    public List<Especialidad> listarActivas(UUID empresaId) {
        return especialidadRepository.findByEmpresaIdAndActivaTrue(empresaId);
    }

    public Especialidad guardar(Especialidad especialidad) {
        validar(especialidad);
        if (especialidadRepository.existsByEmpresaIdAndNombreIgnoreCase(especialidad.getEmpresaId(), especialidad.getNombre())) {
            throw new IllegalArgumentException("Ya existe una especialidad con ese nombre.");
        }
        especialidad.setActiva(true);
        return especialidadRepository.save(especialidad);
    }

    public Especialidad actualizar(UUID empresaId, UUID especialidadId, String nombre, String descripcion) {
        Especialidad especialidad = especialidadRepository.findByIdAndEmpresaId(especialidadId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada para esta empresa."));
        validar(nombre, descripcion);
        if (!especialidad.getNombre().equalsIgnoreCase(nombre.trim())
                && especialidadRepository.existsByEmpresaIdAndNombreIgnoreCase(empresaId, nombre.trim())) {
            throw new IllegalArgumentException("Ya existe una especialidad con ese nombre.");
        }
        especialidad.setNombre(nombre.trim());
        especialidad.setDescripcion(descripcion == null ? "" : descripcion.trim());
        return especialidadRepository.save(especialidad);
    }

    public void cambiarEstado(UUID empresaId, UUID especialidadId, boolean activa) {
        Especialidad especialidad = especialidadRepository.findByIdAndEmpresaId(especialidadId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada para esta empresa."));
        especialidad.setActiva(activa);
        especialidadRepository.save(especialidad);
    }

    public List<Especialidad> buscar(UUID empresaId, String nombre) {
        String filtro = nombre == null ? "" : nombre.trim().toLowerCase();
        return listarPorEmpresa(empresaId).stream()
                .filter(especialidad -> filtro.isBlank() || especialidad.getNombre().toLowerCase().contains(filtro))
                .toList();
    }

    private void validar(Especialidad especialidad) {
        if (especialidad == null) {
            throw new IllegalArgumentException("Completa los datos de la especialidad.");
        }
        validar(especialidad.getNombre(), especialidad.getDescripcion());
    }

    private void validar(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio.");
        }
        if (nombre.trim().length() > 100 || (descripcion != null && descripcion.length() > 2000)) {
            throw new IllegalArgumentException("El texto ingresado supera la longitud permitida.");
        }
    }
}
