package com.citacloud.app.services;

import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProcesadorOutboxNotificaciones {
    private static final Logger LOG = LoggerFactory.getLogger(ProcesadorOutboxNotificaciones.class);
    private final OutboxNotificacionRepository outbox;
    private final EntregaNotificacionRepository entregas;
    private final ConfiguracionNotificacionCitaRepository config;
    private final EmpresaRepository empresas;
    private final EmailProvider email;
    private final WhatsappProvider whatsapp;
    private final MetricasNotificaciones metricas;
    private final ObservationRegistry observaciones;
    private final int max;
    private final TransactionTemplate tx;

    public ProcesadorOutboxNotificaciones(OutboxNotificacionRepository outbox,
                                          EntregaNotificacionRepository entregas,
                                          ConfiguracionNotificacionCitaRepository config,
                                          EmpresaRepository empresas,
                                          EmailProvider email, WhatsappProvider whatsapp,
                                          MetricasNotificaciones metricas,
                                          ObservationRegistry observaciones,
                                          @Value("${app.notifications.worker.max-attempts:3}") int max,
                                          PlatformTransactionManager transactionManager) {
        this.outbox = outbox;
        this.entregas = entregas;
        this.config = config;
        this.empresas = empresas;
        this.email = email;
        this.whatsapp = whatsapp;
        this.metricas = metricas;
        this.observaciones = observaciones;
        this.max = Math.max(1, max);
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelayString = "${app.notifications.worker.delay-ms:15000}")
    public void ejecutar() {
        for (OutboxNotificacion item : outbox.findByEstadoAndProximoIntentoLessThanEqualOrderByCreadoEnAsc(
                "PENDIENTE", LocalDateTime.now(), PageRequest.of(0, 20))) {
            try {
                tx.executeWithoutResult(status -> procesar(item.getId()));
            } catch (Exception exception) {
                LOG.warn("notificationId={} tenant={} status=worker_error errorCode=NOTIFICATION_PROVIDER_ERROR",
                        item.getEntrega().getId(), item.getEmpresaId());
            }
        }
    }

    void procesar(UUID id) {
        OutboxNotificacion item = outbox.findById(id).orElse(null);
        if (item == null || !"PENDIENTE".equals(item.getEstado())) return;
        EntregaNotificacion entrega = item.getEntrega();
        Observation.createNotStarted("notification.process", observaciones)
                .lowCardinalityKeyValue("channel", entrega.getCanal().toLowerCase())
                .lowCardinalityKeyValue("event", "appointment_created")
                .observe(() -> procesar(item, entrega));
    }

    private void procesar(OutboxNotificacion item, EntregaNotificacion entrega) {
        int intento = item.getIntentos() + 1;
        item.setIntentos(intento);
        entrega.setCantidadIntentos(intento);
        entrega.setEstado("PROCESANDO");
        Timer.Sample muestra = metricas.iniciar();
        try {
            ConfiguracionNotificacionCita configuracion = config
                    .findByEmpresaIdAndEvento(entrega.getEmpresaId(), entrega.getEvento())
                    .orElseThrow(() -> new IllegalStateException("EMAIL_CHANNEL_NOT_CONFIGURED"));
            String referencia;
            if ("EMAIL".equals(entrega.getCanal())) {
                String nombreClinica = empresas.findById(entrega.getEmpresaId())
                        .map(Empresa::getNombre).orElse("CitaCloud");
                referencia = email.enviar(entrega.getDestinatario(), entrega.getAsunto(),
                        entrega.getMensajeRenderizado(), nombreClinica).referencia();
            } else {
                referencia = whatsapp.enviar(entrega.getDestinatario(), entrega.getMensajeRenderizado(),
                        configuracion.getWhatsappTemplateId()).referencia();
            }
            entrega.setEstado("ENVIADO");
            entrega.setReferenciaExterna(referencia);
            entrega.setEnviadaEn(LocalDateTime.now());
            entrega.setCodigoFallo(null);
            entrega.setMotivoFallo(null);
            item.setEstado("PROCESADO");
            item.setProcesadoEn(LocalDateTime.now());
            metricas.enviado(entrega.getCanal(), muestra);
            LOG.info("notificationId={} appointmentId={} tenant={} channel={} status=sent attempts={}",
                    entrega.getId(), entrega.getEntidadId(), entrega.getEmpresaId(), entrega.getCanal(), intento);
        } catch (Exception exception) {
            boolean reintentar = intento < max && reintentable(exception);
            entrega.setEstado(reintentar ? "PENDIENTE" : "FALLIDO");
            entrega.setCodigoFallo(reintentar ? "NOTIFICATION_PROVIDER_RETRY" : "NOTIFICATION_PROVIDER_ERROR");
            entrega.setMotivoFallo("El proveedor no pudo procesar el mensaje.");
            item.setEstado(reintentar ? "PENDIENTE" : "FALLIDO");
            item.setProximoIntento(LocalDateTime.now().plusMinutes((long) intento * 2));
            if (reintentar) metricas.reintento(entrega.getCanal(), muestra);
            else metricas.fallido(entrega.getCanal(), muestra);
            LOG.warn("notificationId={} appointmentId={} tenant={} channel={} status={} attempts={} errorCode={} exception={}",
                    entrega.getId(), entrega.getEntidadId(), entrega.getEmpresaId(), entrega.getCanal(),
                    entrega.getEstado(), intento, entrega.getCodigoFallo(), exception.getClass().getSimpleName());
        }
        entrega.setActualizadaEn(LocalDateTime.now());
        entregas.save(entrega);
        outbox.save(item);
    }

    private boolean reintentable(Exception exception) {
        String mensaje = exception.getMessage() == null ? "" : exception.getMessage();
        return !mensaje.contains("NOT_CONFIGURED") && !mensaje.contains("HTTP_400")
                && !mensaje.contains("HTTP_401") && !mensaje.contains("HTTP_403")
                && !mensaje.contains("HTTP_404");
    }
}
