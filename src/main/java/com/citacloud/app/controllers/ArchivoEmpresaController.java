package com.citacloud.app.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/** Entrega exclusivamente los logos almacenados para las empresas. */
@RestController
@RequestMapping("/uploads/empresas")
public class ArchivoEmpresaController {

    private static final Set<String> ARCHIVOS_PERMITIDOS = Set.of("logo.png", "logo.jpg", "logo.webp");
    private final Path directorioCarga;

    public ArchivoEmpresaController(@Value("${app.upload-dir:uploads}") String directorioCarga) {
        this.directorioCarga = Paths.get(directorioCarga).toAbsolutePath().normalize();
    }

    @GetMapping("/{empresaId}/{archivo:.+}")
    public ResponseEntity<Resource> obtenerLogo(@PathVariable UUID empresaId, @PathVariable String archivo) throws IOException {
        if (!ARCHIVOS_PERMITIDOS.contains(archivo)) {
            return ResponseEntity.notFound().build();
        }
        Path archivoLogo = directorioCarga.resolve("empresas").resolve(empresaId.toString()).resolve(archivo).normalize();
        if (!archivoLogo.startsWith(directorioCarga) || !Files.isRegularFile(archivoLogo)) {
            return ResponseEntity.notFound().build();
        }
        String tipoDetectado = Files.probeContentType(archivoLogo);
        MediaType tipo = tipoDetectado == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(tipoDetectado);
        return ResponseEntity.ok()
                .contentType(tipo)
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(archivoLogo));
    }
}
