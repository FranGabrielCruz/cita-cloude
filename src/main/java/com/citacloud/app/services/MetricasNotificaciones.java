package com.citacloud.app.services;

import com.citacloud.app.repositories.OutboxNotificacionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/** Métricas técnicas agregadas; nunca utiliza destinatarios, usuarios ni tenants como labels. */
@Component
public class MetricasNotificaciones {
    private static final String EMAIL = "EMAIL";
    private final MeterRegistry registry;
    private final Map<String, Counter> enviados;
    private final Map<String, Counter> fallidos;
    private final Map<String, Counter> omitidos;
    private final Map<String, Counter> reintentos;
    private final Map<String, Timer> duraciones;

    public MetricasNotificaciones(MeterRegistry registry, OutboxNotificacionRepository outbox) {
        this.registry = registry;
        enviados = Map.of(EMAIL, counter("email.sent"), "WHATSAPP", counter("whatsapp.sent"));
        fallidos = Map.of(EMAIL, counter("email.failed"), "WHATSAPP", counter("whatsapp.failed"));
        omitidos = Map.of(EMAIL, counter("email.skipped"), "WHATSAPP", counter("whatsapp.skipped"));
        reintentos = Map.of(EMAIL, counter("email.retry"), "WHATSAPP", counter("whatsapp.retry"));
        duraciones = Map.of(EMAIL, timer("email.send.duration"), "WHATSAPP", timer("whatsapp.send.duration"));
        for (String estado : new String[]{"PENDIENTE", "PROCESADO", "FALLIDO"}) {
            Gauge.builder("notification.queue.depth", outbox,
                            repo -> valorSeguro(() -> repo.countByEstado(estado)))
                    .tag("status", estado.toLowerCase(Locale.ROOT)).register(registry);
        }
        Gauge.builder("notification.queue.oldest.age", outbox, repo -> valorSeguro(() -> repo
                        .findFirstByEstadoOrderByCreadoEnAsc("PENDIENTE")
                        .map(item -> (double) Math.max(0, Duration.between(item.getCreadoEn(), LocalDateTime.now()).toSeconds()))
                        .orElse(0d)))
                .baseUnit("seconds").register(registry);
    }

    public Timer.Sample iniciar() { return Timer.start(registry); }
    public void enviado(String canal, Timer.Sample muestra) { enviados.get(normalizar(canal)).increment(); detener(canal, muestra); }
    public void fallido(String canal, Timer.Sample muestra) { fallidos.get(normalizar(canal)).increment(); detener(canal, muestra); }
    public void reintento(String canal, Timer.Sample muestra) { reintentos.get(normalizar(canal)).increment(); detener(canal, muestra); }
    public void omitido(String canal) { omitidos.get(normalizar(canal)).increment(); }
    private void detener(String canal, Timer.Sample muestra) { if (muestra != null) muestra.stop(duraciones.get(normalizar(canal))); }
    private Counter counter(String nombre) { return Counter.builder(nombre).register(registry); }
    private Timer timer(String nombre) { return Timer.builder(nombre).publishPercentileHistogram().register(registry); }
    private String normalizar(String canal) { return "WHATSAPP".equals(canal) ? "WHATSAPP" : EMAIL; }
    private double valorSeguro(Consulta consulta) { try { return consulta.obtener(); } catch (RuntimeException ignored) { return Double.NaN; } }
    @FunctionalInterface private interface Consulta { double obtener(); }
}
