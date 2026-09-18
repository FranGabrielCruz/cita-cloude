package com.citacloud.app.dto;

import java.time.OffsetDateTime;

/** Contrato público y uniforme para los errores de la API. */
public record ApiErrorResponse(
        boolean success,
        Error error,
        String requestId,
        OffsetDateTime timestamp) {

    public record Error(String code, String message, Object details) {
    }
}
