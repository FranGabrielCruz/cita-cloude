package com.citacloud.app.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MetricasAlmacenamiento {
    private final MeterRegistry registry;
    private final Counter escrituras;
    private final Counter fallos;
    private final DistributionSummary bytes;
    private final Timer duracion;

    public MetricasAlmacenamiento(MeterRegistry registry) {
        this.registry = registry;
        escrituras = registry.counter("storage.write");
        fallos = registry.counter("storage.failed");
        bytes = DistributionSummary.builder("storage.write.size").baseUnit("bytes").register(registry);
        duracion = Timer.builder("storage.write.duration").publishPercentileHistogram().register(registry);
    }

    public Timer.Sample iniciar() { return Timer.start(registry); }
    public void escrito(Timer.Sample muestra, long cantidadBytes) {
        escrituras.increment();
        bytes.record(Math.max(0, cantidadBytes));
        muestra.stop(duracion);
    }
    public void fallo(Timer.Sample muestra) { fallos.increment(); muestra.stop(duracion); }
}
