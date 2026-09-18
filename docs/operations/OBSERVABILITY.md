# Observabilidad

## Componentes actuales

```text
Cita Cloud -> Micrometer/OpenTelemetry -> OTLP Collector
          \-> /metrics -> Prometheus -> Grafana
          \-> logs con requestId/traceId
Docker -> cAdvisor -> Prometheus
```

OpenTelemetry instrumenta y transporta trazas; Prometheus almacena métricas; Grafana las visualiza. Sentry gestionaría errores/diagnóstico, pero no está integrado actualmente. Auditoría funcional y observabilidad técnica son fuentes distintas.

## Endpoints y paneles

- `/health/live`, `/health/ready`: disponibilidad.
- `/metrics`: formato Prometheus.
- Prometheus `:9090`; Grafana `:3000`; cAdvisor `:8082`.
- Collector recibe OTLP gRPC `4317` y HTTP `4318`; no tiene interfaz web.

Métricas personalizadas cubren autenticación, pagos, correo, WhatsApp, generación PDF y almacenamiento. Alertas y dashboard se versionan en `observability/`.

## Investigación

`Reporte del usuario -> obtener requestId -> buscar log/evento -> obtener traceId -> revisar recorrido y métricas -> identificar componente -> corregir/verificar`

No se incluyen nombres de pacientes, contenido clínico, tokens o contraseñas en etiquetas/logs. El muestreo, la retención y el acceso deben ajustarse por ambiente. Consulte el [Runbook](RUNBOOK.md).
