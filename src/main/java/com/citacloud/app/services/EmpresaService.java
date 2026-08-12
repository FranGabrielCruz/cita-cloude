package com.citacloud.app.services;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Value("${app.upload-dir:uploads}")
    private String directorioCarga;

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

    /**
     * Guarda el archivo fuera de la base de datos y conserva en ella únicamente
     * la ruta pública asociada a la empresa.
     */
    public Empresa guardarLogo(UUID empresaId, InputStream contenido, String nombreArchivo, String tipoContenido) {
        if (contenido == null) {
            throw new IllegalArgumentException("Seleccione una imagen para el logo.");
        }
        String extension = extensionPermitida(tipoContenido, nombreArchivo);
        try {
            byte[] datos = contenido.readAllBytes();
            if (datos.length == 0) {
                throw new IllegalArgumentException("El archivo del logo está vacío.");
            }
            if (datos.length > 2 * 1024 * 1024) {
                throw new IllegalArgumentException("El logo no puede superar 2 MB.");
            }

            Empresa empresa = buscar(empresaId);
            Path base = Paths.get(directorioCarga).toAbsolutePath().normalize();
            Path carpetaEmpresa = base.resolve("empresas").resolve(empresaId.toString()).normalize();
            if (!carpetaEmpresa.startsWith(base)) {
                throw new IllegalArgumentException("No se pudo guardar el logo.");
            }
            Files.createDirectories(carpetaEmpresa);
            Path destino = carpetaEmpresa.resolve("logo." + extension);
            Files.write(destino, datos, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            empresa.setLogoUrl("/uploads/empresas/" + empresaId + "/logo." + extension + "?v=" + System.currentTimeMillis());
            empresa.setActualizadoEn(LocalDateTime.now());
            return empresaRepository.save(empresa);
        } catch (IOException ex) {
            throw new IllegalArgumentException("No fue posible guardar el logo.", ex);
        }
    }

    private String extensionPermitida(String tipoContenido, String nombreArchivo) {
        String tipo = tipoContenido == null ? "" : tipoContenido.toLowerCase();
        return switch (tipo) {
            case "image/png" -> "png";
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("El logo debe ser una imagen PNG, JPG o WEBP.");
        };
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
