package com.citacloud.app.controllers;

import com.citacloud.app.models.Documento;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.services.DocumentoService;
import com.citacloud.app.services.AuditoriaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {
    private final DocumentoService servicio;
    private final AuditoriaService auditoria;

    public DocumentoController(DocumentoService servicio, AuditoriaService auditoria) {
        this.servicio = servicio;
        this.auditoria = auditoria;
    }

    @GetMapping("/{id}/ver")
    public ResponseEntity<FileSystemResource> ver(@PathVariable UUID id) {
        return entregar(id, false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileSystemResource> descargar(@PathVariable UUID id) {
        return entregar(id, true);
    }

    private ResponseEntity<FileSystemResource> entregar(UUID id, boolean descargar) {
        var usuario = AuthService.getAuthenticatedUser();
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            Documento documento = servicio.obtener(usuario.getEmpresaId(), id);
            Path ruta = servicio.ruta(documento);
            if (!Files.isRegularFile(ruta)) return ResponseEntity.notFound().build();
            auditoria.registrar(usuario.getEmpresaId(), usuario.getUsuarioId(), "DOCUMENTOS",
                    descargar ? "DOCUMENT_DOWNLOADED" : "DOCUMENT_VIEWED", "DOCUMENTO", documento.getId(),
                    documento.getNombre(), documento.getPaciente().getId(), java.util.List.of(), "SUCCESS", null, true);
            String archivo = documento.getNombreArchivo().replace("\"", "");
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(documento.getTipo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, (descargar ? "attachment" : "inline")
                            + "; filename=\"" + archivo + "\"")
                    .body(new FileSystemResource(ruta));
        } catch (Exception error) {
            auditoria.registrar(usuario.getEmpresaId(), usuario.getUsuarioId(), "DOCUMENTOS", "DOCUMENT_ACCESS",
                    "DOCUMENTO", id, null, null, java.util.List.of(), "DENIED", "Documento no disponible o sin autorización", true);
            return ResponseEntity.notFound().build();
        }
    }
}
