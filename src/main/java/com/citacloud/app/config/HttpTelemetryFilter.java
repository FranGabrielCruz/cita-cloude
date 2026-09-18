package com.citacloud.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Registra únicamente solicitudes lentas o fallidas usando rutas normalizadas. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class HttpTelemetryFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpTelemetryFilter.class);
    private final long umbralLentoMs;

    public HttpTelemetryFilter(@Value("${app.observability.slow-http-threshold-ms:1000}") long umbralLentoMs) {
        this.umbralLentoMs = Math.max(1, umbralLentoMs);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long inicio = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
            if (response.getStatus() >= 400 || duracionMs >= umbralLentoMs) {
                Object patron = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                String ruta = patron == null ? "unmatched" : patron.toString();
                MDC.put("route", ruta);
                MDC.put("method", request.getMethod());
                MDC.put("status", Integer.toString(response.getStatus()));
                MDC.put("durationMs", Long.toString(duracionMs));
                try {
                    if (response.getStatus() >= 500) {
                        log.error("http_request_failed");
                    } else if (response.getStatus() >= 400) {
                        log.warn("http_request_rejected");
                    } else {
                        log.warn("http_request_slow");
                    }
                } finally {
                    MDC.remove("route");
                    MDC.remove("method");
                    MDC.remove("status");
                    MDC.remove("durationMs");
                }
            }
        }
    }
}
