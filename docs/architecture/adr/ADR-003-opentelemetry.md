# ADR-003 — OpenTelemetry y Prometheus

**Estado:** Aceptado  
**Fecha:** 08/09/2026

## Contexto

Se necesita correlacionar solicitudes y medir disponibilidad, latencia y fallos sin incluir datos sensibles.

## Decisión

Instrumentar Spring Boot con Micrometer, bridge OpenTelemetry y OTLP. Publicar métricas Prometheus y usar `requestId`/`traceId` en logs. Grafana visualiza métricas.

## Alternativas consideradas

- Logs solamente: insuficientes para tendencias y alertas.
- SDK propietario único: crea acoplamiento y limita portabilidad.

## Consecuencias

La telemetría es portable, pero requiere Collector, almacenamiento, muestreo, retención y sanitización. Sentry sigue pendiente y no se declara implementado.
