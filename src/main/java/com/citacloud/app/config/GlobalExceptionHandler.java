package com.citacloud.app.config;

import com.citacloud.app.dto.ApiErrorResponse;
import com.citacloud.app.exceptions.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/** Traduce las excepciones de los endpoints REST al contrato público de Cita Cloud. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @Autowired(required = false)
    private Tracer tracer;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> application(ApplicationException exception, HttpServletRequest request) {
        log.warn("Error controlado code={} requestId={} traceId={} method={} uri={}", exception.getCode(), requestId(request),
                traceId(), request.getMethod(), request.getRequestURI());
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), exception.getDetails(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<Map<String, String>> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldDetail)
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Algunos campos contienen errores.", details, request);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> invalidRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "La solicitud contiene datos inválidos.", null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> duplicate(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Conflicto de integridad requestId={} method={} uri={}", requestId(request), request.getMethod(), request.getRequestURI());
        return response(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Ya existe un registro con esos datos.", null, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> concurrent(ObjectOptimisticLockingFailureException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", "El registro fue actualizado por otro usuario. Intenta nuevamente.", null, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> responseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : status == HttpStatus.FORBIDDEN ? "FORBIDDEN" : "REQUEST_ERROR";
        String message = status == HttpStatus.UNAUTHORIZED ? "Tu sesión ha expirado. Inicia sesión nuevamente."
                : status == HttpStatus.FORBIDDEN ? "No tienes permisos para realizar esta acción."
                : "No se pudo completar la solicitud.";
        return response(status, code, message, null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Error inesperado requestId={} traceId={} method={} uri={} exceptionType={}",
                requestId(request), traceId(), request.getMethod(), request.getRequestURI(),
                exception.getClass().getSimpleName(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado. Intenta nuevamente.", null, request);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message, Object details,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(false,
                new ApiErrorResponse.Error(code, message, details), requestId(request), OffsetDateTime.now(ZoneOffset.UTC)));
    }

    private String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        return requestId == null ? "unknown" : requestId.toString();
    }

    private String traceId() {
        return tracer == null || tracer.currentSpan() == null
                ? "unknown" : tracer.currentSpan().context().traceId();
    }

    private Map<String, String> fieldDetail(FieldError error) {
        return Map.of("field", error.getField(), "code", "INVALID_VALUE",
                "message", error.getDefaultMessage() == null ? "El valor no es válido." : error.getDefaultMessage());
    }
}
