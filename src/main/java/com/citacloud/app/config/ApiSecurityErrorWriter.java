package com.citacloud.app.config;

import com.citacloud.app.dto.ApiErrorResponse;
import com.citacloud.app.services.MetricasSeguridad;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Escribe errores de autenticación/autorización con el mismo contrato de la API. */
@Component
public class ApiSecurityErrorWriter {
    private final ObjectMapper objectMapper;
    private final MetricasSeguridad metricas;

    public ApiSecurityErrorWriter(ObjectMapper objectMapper, MetricasSeguridad metricas) {
        this.objectMapper = objectMapper;
        this.metricas = metricas;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status) throws IOException {
        Object attribute = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        String requestId = attribute == null ? "unknown" : attribute.toString();
        String code = status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : "FORBIDDEN";
        if (status == HttpStatus.UNAUTHORIZED) metricas.noAutorizado();
        else metricas.prohibido();
        String message = status == HttpStatus.UNAUTHORIZED ? "Tu sesión ha expirado. Inicia sesión nuevamente."
                : "No tienes permisos para realizar esta acción.";
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(false,
                new ApiErrorResponse.Error(code, message, null), requestId, OffsetDateTime.now(ZoneOffset.UTC)));
    }
}
