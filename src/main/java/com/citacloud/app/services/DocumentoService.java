package com.citacloud.app.services;

import com.citacloud.app.models.Documento;
import com.citacloud.app.models.Paciente;
import com.citacloud.app.repositories.DocumentoRepository;
import com.citacloud.app.repositories.PacienteRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentoService {
    private static final long MAXIMO_BYTES = 10 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("application/pdf", "image/png", "image/jpeg");
    private final DocumentoRepository repositorio;
    private final PacienteRepository pacientes;
    private final AuditoriaService auditoria;
    private final Path raiz;

    public DocumentoService(DocumentoRepository repositorio, PacienteRepository pacientes, AuditoriaService auditoria,
                            @Value("${app.upload-dir:uploads}") String directorio) {
        this.repositorio = repositorio;
        this.pacientes = pacientes;
        this.auditoria = auditoria;
        raiz = Paths.get(directorio).toAbsolutePath().normalize();
    }

    public List<Documento> listar(UUID empresaId) {
        return repositorio.findByEmpresaIdOrderByFechaDesc(empresaId);
    }

    @Transactional
    public Documento subir(UUID empresaId, UUID usuarioId, UUID pacienteId, String nombre, String descripcion,
                           String archivo, String mime, InputStream datos, long tamano) throws IOException {
        validarGestion(empresaId, usuarioId);
        if (datos == null || tamano <= 0 || tamano > MAXIMO_BYTES || !TIPOS_PERMITIDOS.contains(mime)) {
            throw new IllegalArgumentException("Solo PDF, PNG o JPG de hasta 10 MB.");
        }
        Paciente paciente = pacientes.findByIdAndEmpresaId(pacienteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para esta empresa."));
        String extension = mime.equals("application/pdf") ? ".pdf" : mime.equals("image/png") ? ".png" : ".jpg";
        Path carpeta = raiz.resolve("documentos").resolve(empresaId.toString()).normalize();
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(UUID.randomUUID() + extension).normalize();
        if (!destino.startsWith(raiz)) throw new IllegalArgumentException("Ruta no válida.");
        Files.copy(datos, destino, StandardCopyOption.REPLACE_EXISTING);

        Documento documento = new Documento();
        documento.setEmpresaId(empresaId);
        documento.setPaciente(paciente);
        documento.setUsuarioId(usuarioId);
        documento.setNombre(limpiar(nombre).isBlank() ? archivo : limpiar(nombre));
        documento.setDescripcion(limpiar(descripcion));
        documento.setNombreArchivo(archivo);
        documento.setRutaArchivo("documentos/" + empresaId + "/" + destino.getFileName());
        documento.setTipo(mime);
        documento.setEstado("ACTIVO");
        documento.setOrigen("MANUAL");
        documento = repositorio.save(documento);
        auditoria.registrar(empresaId, usuarioId, "DOCUMENTOS", "DOCUMENT_CREATED", "DOCUMENTO", documento.getId(), documento.getNombre());
        return documento;
    }

    @Transactional
    public Documento editarMetadatos(UUID empresaId, UUID usuarioId, UUID documentoId, String titulo,
                                     String descripcion, LocalDateTime fechaDocumento) {
        validarGestion(empresaId, usuarioId);
        Documento documento = obtenerAdministrable(empresaId, documentoId);
        String tituloLimpio = limpiar(titulo);
        if (tituloLimpio.isBlank()) throw new IllegalArgumentException("El título del documento es obligatorio.");
        String anterior = documento.getNombre();
        documento.setNombre(tituloLimpio);
        documento.setDescripcion(limpiar(descripcion));
        if (fechaDocumento != null) documento.setFecha(fechaDocumento);
        documento.setActualizadoEn(LocalDateTime.now());
        documento = repositorio.save(documento);
        auditoria.registrar(empresaId, usuarioId, "DOCUMENTOS", "DOCUMENT_UPDATED", "DOCUMENTO", documento.getId(),
                "Título: " + anterior + " → " + documento.getNombre());
        return documento;
    }

    @Transactional
    public void archivar(UUID empresaId, UUID usuarioId, UUID documentoId, String motivo) {
        validarGestion(empresaId, usuarioId);
        Documento documento = obtenerAdministrable(empresaId, documentoId);
        String motivoLimpio = limpiar(motivo);
        if (motivoLimpio.isBlank()) throw new IllegalArgumentException("El motivo de archivo es obligatorio.");
        documento.setEstado("ARCHIVADO");
        documento.setArchivadoEn(LocalDateTime.now());
        documento.setArchivadoPor(usuarioId);
        documento.setMotivoArchivo(motivoLimpio);
        documento.setActualizadoEn(LocalDateTime.now());
        repositorio.save(documento);
        auditoria.registrar(empresaId, usuarioId, "DOCUMENTOS", "DOCUMENT_ARCHIVED", "DOCUMENTO", documento.getId(), motivoLimpio);
    }

    public Documento obtener(UUID empresaId, UUID id) {
        return repositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado para esta empresa."));
    }

    public Path ruta(Documento documento) {
        Path ruta = raiz.resolve(documento.getRutaArchivo()).normalize();
        if (!ruta.startsWith(raiz)) throw new IllegalArgumentException("Ruta no válida.");
        return ruta;
    }

    private Documento obtenerAdministrable(UUID empresaId, UUID documentoId) {
        Documento documento = obtener(empresaId, documentoId);
        if (!"ACTIVO".equals(documento.getEstado())) {
            throw new IllegalArgumentException("El documento ya se encuentra archivado.");
        }
        if (!"MANUAL".equals(documento.getOrigen())) {
            throw new IllegalArgumentException("Este documento fue generado por CitaCloud y debe gestionarse desde su módulo de origen.");
        }
        return documento;
    }

    private void validarGestion(UUID empresaId, UUID usuarioId) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null || !empresaId.equals(usuario.getEmpresaId()) || !usuarioId.equals(usuario.getUsuarioId())) {
            throw new IllegalArgumentException("No tienes permisos para gestionar este documento.");
        }
        boolean autorizado = usuario.getAuthorities().stream().anyMatch(authority ->
                "ROLE_ADMINISTRADOR".equals(authority.getAuthority()) || "ROLE_SUPERADMIN".equals(authority.getAuthority())
                        || "MENU_DOCUMENTOS".equals(authority.getAuthority()));
        if (!autorizado) throw new IllegalArgumentException("No tienes permisos para gestionar documentos.");
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
