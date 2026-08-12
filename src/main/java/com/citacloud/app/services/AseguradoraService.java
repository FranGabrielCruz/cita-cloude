package com.citacloud.app.services;

import com.citacloud.app.models.Aseguradora;
import com.citacloud.app.repositories.AseguradoraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
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

    public List<Aseguradora> buscar(UUID empresaId, String nombre, Boolean activa) {
        String nombreNormalizado = normalizar(nombre);
        return aseguradoraRepository.findByEmpresaId(empresaId).stream()
                .filter(aseguradora -> nombreNormalizado.isBlank()
                        || aseguradora.getNombre().toLowerCase(Locale.ROOT).contains(nombreNormalizado))
                .filter(aseguradora -> activa == null || activa.equals(aseguradora.getActiva()))
                .toList();
    }

    public Aseguradora crear(UUID empresaId, String nombre, String telefono) {
        Aseguradora aseguradora = new Aseguradora();
        aseguradora.setEmpresaId(empresaId);
        actualizarDatos(aseguradora, empresaId, nombre, telefono);
        aseguradora.setActiva(true);
        return aseguradoraRepository.save(aseguradora);
    }

    public Aseguradora actualizar(UUID empresaId, UUID id, String nombre, String telefono) {
        Aseguradora aseguradora = obtener(empresaId, id);
        actualizarDatos(aseguradora, empresaId, nombre, telefono);
        return aseguradoraRepository.save(aseguradora);
    }

    public Aseguradora cambiarEstado(UUID empresaId, UUID id, boolean activa) {
        Aseguradora aseguradora = obtener(empresaId, id);
        aseguradora.setActiva(activa);
        return aseguradoraRepository.save(aseguradora);
    }

    private void actualizarDatos(Aseguradora aseguradora, UUID empresaId, String nombre, String telefono) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isBlank()) {
            throw new IllegalArgumentException("El nombre de la aseguradora es obligatorio.");
        }
        boolean duplicada = aseguradoraRepository.findByEmpresaId(empresaId).stream()
                .anyMatch(item -> item.getNombre().equalsIgnoreCase(nombreLimpio) && !item.getId().equals(aseguradora.getId()));
        if (duplicada) {
            throw new IllegalArgumentException("Ya existe una aseguradora con ese nombre.");
        }
        aseguradora.setNombre(nombreLimpio);
        aseguradora.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
    }

    private Aseguradora obtener(UUID empresaId, UUID id) {
        return aseguradoraRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontr\u00f3 la aseguradora solicitada."));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
