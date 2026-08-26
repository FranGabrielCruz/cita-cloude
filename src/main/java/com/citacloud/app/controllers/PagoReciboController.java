package com.citacloud.app.controllers;

import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.PagoService;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/pagos")
@PermitAll
public class PagoReciboController {
    private final PagoService pagos;
    public PagoReciboController(PagoService pagos){this.pagos=pagos;}
    @GetMapping(value="/{id}/recibo",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> recibo(@PathVariable UUID id){TenantUserDetails usuario=AuthService.getAuthenticatedUser();if(usuario==null)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=recibo-"+id+".pdf").body(pagos.generarRecibo(usuario.getEmpresaId(),id));}
}
