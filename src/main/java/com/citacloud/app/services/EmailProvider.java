package com.citacloud.app.services;

import java.util.List;

public interface EmailProvider {
    boolean configurado();

    default List<String> configuracionFaltante() {
        return configurado() ? List.of() : List.of("configuración técnica SMTP");
    }

    Resultado enviar(String destinatario, String asunto, String mensaje, String nombreClinica);

    record Resultado(String proveedor, String referencia) {}
}
