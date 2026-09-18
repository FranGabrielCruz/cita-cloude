package com.citacloud.app.controllers;

import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.FacturaPdfService;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/facturas")
@PermitAll
public class FacturaPdfController {
    private final FacturaPdfService pdf;
    public FacturaPdfController(FacturaPdfService pdf) { this.pdf = pdf; }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> factura(@PathVariable UUID id) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=factura-" + id + ".pdf").body(pdf.generar(usuario.getEmpresaId(), id));
    }
}
