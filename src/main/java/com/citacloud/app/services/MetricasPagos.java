package com.citacloud.app.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/** Telemetría agregada del flujo de cobro; no registra importes, pacientes ni referencias. */
@Component
public class MetricasPagos {
    private final MeterRegistry registry;
    private final Counter intentados;
    private final Counter aprobados;
    private final Counter rechazados;
    private final Counter errores;
    private final Timer duracion;

    public MetricasPagos(MeterRegistry registry) {
        this.registry = registry;
        intentados = registry.counter("payment.attempted");
        aprobados = registry.counter("payment.approved");
        rechazados = registry.counter("payment.rejected");
        errores = registry.counter("payment.error");
        duracion = Timer.builder("payment.processing.duration").publishPercentileHistogram().register(registry);
    }

    public Timer.Sample iniciar() { intentados.increment(); return Timer.start(registry); }
    public void aprobado(Timer.Sample muestra) { aprobados.increment(); muestra.stop(duracion); }
    public void rechazado(Timer.Sample muestra) { rechazados.increment(); muestra.stop(duracion); }
    public void error(Timer.Sample muestra) { errores.increment(); muestra.stop(duracion); }
}
