package com.citacloud.app.services;

import com.citacloud.app.repositories.OutboxNotificacionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MetricasTecnicasTest {
    @Test
    void registraSeguridadSinEtiquetasSensibles() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricasSeguridad metricas = new MetricasSeguridad(registry);

        metricas.accesoCorrecto();
        metricas.accesoFallido();
        metricas.noAutorizado();
        metricas.prohibido();

        assertThat(registry.counter("auth.login.success").count()).isEqualTo(1);
        assertThat(registry.counter("auth.login.failure").count()).isEqualTo(1);
        assertThat(registry.getMeters()).allSatisfy(meter ->
                assertThat(meter.getId().getTags()).isEmpty());
    }

    @Test
    void registraCanalesConNombresControlados() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricasNotificaciones metricas = new MetricasNotificaciones(
                registry, mock(OutboxNotificacionRepository.class));

        metricas.enviado("EMAIL", metricas.iniciar());
        metricas.fallido("WHATSAPP", metricas.iniciar());
        metricas.omitido("EMAIL");

        assertThat(registry.counter("email.sent").count()).isEqualTo(1);
        assertThat(registry.counter("whatsapp.failed").count()).isEqualTo(1);
        assertThat(registry.counter("email.skipped").count()).isEqualTo(1);
    }

    @Test
    void registraCicloDePago() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricasPagos metricas = new MetricasPagos(registry);

        metricas.aprobado(metricas.iniciar());
        metricas.rechazado(metricas.iniciar());

        assertThat(registry.counter("payment.attempted").count()).isEqualTo(2);
        assertThat(registry.counter("payment.approved").count()).isEqualTo(1);
        assertThat(registry.counter("payment.rejected").count()).isEqualTo(1);
    }
}
