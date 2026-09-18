package com.citacloud.app.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Métricas de seguridad agregadas, sin usuarios, direcciones IP ni tenants como etiquetas. */
@Component
public class MetricasSeguridad {
    private final Counter accesosCorrectos;
    private final Counter accesosFallidos;
    private final Counter noAutorizados;
    private final Counter prohibidos;

    public MetricasSeguridad(MeterRegistry registry) {
        accesosCorrectos = registry.counter("auth.login.success");
        accesosFallidos = registry.counter("auth.login.failure");
        noAutorizados = registry.counter("auth.unauthorized");
        prohibidos = registry.counter("auth.forbidden");
    }

    public void accesoCorrecto() { accesosCorrectos.increment(); }
    public void accesoFallido() { accesosFallidos.increment(); }
    public void noAutorizado() { noAutorizados.increment(); }
    public void prohibido() { prohibidos.increment(); }
}
