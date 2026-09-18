# Observabilidad técnica de CitaCloud

Esta carpeta contiene la pila técnica separada de la interfaz de CitaCloud:

- Prometheus recopila `/metrics` cada 15 segundos.
- Grafana carga automáticamente el panel **CITA CLOUD — API**.
- OpenTelemetry Collector recibe trazas OTLP.
- cAdvisor aporta métricas de los contenedores Docker.

## Inicio local con Docker Desktop

1. Copie `.env.example` a `.env` y complete la base de datos y una contraseña segura para Grafana.
2. Ejecute `docker compose -f docker-compose.desktop.yml up -d --build`.
3. Compruebe CitaCloud en `http://localhost:8081/health/ready`.
4. Abra Grafana en `http://localhost:3000` y Prometheus en `http://localhost:9090`.

Los puertos de monitoreo se publican solamente en `127.0.0.1`. En producción deben permanecer detrás de una red privada, autenticación y TLS.

## Pruebas ejecutando CitaCloud desde IntelliJ IDEA

Cuando la aplicación se ejecuta directamente desde IntelliJ, levante únicamente la infraestructura técnica:

`docker compose -f docker-compose.observability-ide.yml up -d`

Prometheus consultará CitaCloud mediante `host.docker.internal:8080`. Configure en la ejecución de IntelliJ las variables `OBSERVABILITY_TRACING_ENABLED=true`, `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4318/v1/traces`, `OTEL_SERVICE_NAME=cita-cloud-api` y `DEPLOYMENT_ENVIRONMENT=development`.

## Endpoints de la aplicación

- `/health`: estado general mínimo.
- `/health/live`: vida del proceso.
- `/health/ready`: disponibilidad, base de datos y disco.
- `/metrics`: métricas Prometheus.

## Protección de datos

Las métricas usan rutas normalizadas y etiquetas de cardinalidad controlada. No incluyen nombre de paciente, correo, teléfono, documento, usuario, IP, tenant ni texto clínico. Los logs de producción son JSON e incluyen `requestId`, `traceId` y `spanId`; tampoco deben registrar secretos ni contenido de notificaciones.

## Trazas y extensiones

El muestreo se controla con `OTEL_TRACES_SAMPLER_ARG`. El valor recomendado inicial es `0.20` en staging y uno menor en producción según el tráfico. El Collector usa temporalmente el exportador `debug`; para retención de trazas se puede reemplazar por Tempo. La misma arquitectura permite añadir Loki para logs y Sentry para excepciones sin cambiar la UI de CitaCloud.

## Alertas incluidas

Prometheus evalúa indisponibilidad, tasa alta de errores, latencia p95, atraso de notificaciones, fallos de correo y saturación del pool de conexiones. Para enviar avisos externos se debe conectar Alertmanager al canal operativo de la clínica.
