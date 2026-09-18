# Configuración

Los valores sensibles se suministran desde el IDE, variables del sistema o gestor de secretos. Los ejemplos vacíos significan “definir externamente”, no ausencia de protección.

| Variable | Propósito | Obligatoria | Ambientes | Ejemplo seguro |
|---|---|:---:|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | Sí | Todos | `local` |
| `DB_URL` | URL PostgreSQL | Sí | Todos | `jdbc:postgresql://localhost:5432/cita_cloud` |
| `DB_USERNAME` | Usuario DB | Sí | Todos | `app_user` |
| `DB_PASSWORD` | Secreto DB | Sí | Todos | `<secret>` |
| `MAIL_HOST` | Host SMTP | Si correo | Todos | `smtp.example.test` |
| `MAIL_PORT` | Puerto SMTP | Si correo | Todos | `587` |
| `MAIL_USERNAME` | Usuario SMTP | Si correo | Todos | `no-reply@example.test` |
| `MAIL_PASSWORD` | Secreto SMTP | Si correo | Todos | `<secret>` |
| `MAIL_SMTP_AUTH` | Autenticación SMTP | Si correo | Todos | `true` |
| `MAIL_STARTTLS` | STARTTLS | Si correo | Todos | `true` |
| `NOTIFICATIONS_EMAIL_CONFIGURED` | Habilita base técnica de correo | No | Todos | `false` |
| `NOTIFICATIONS_EMAIL_FROM` | Dirección visible del remitente | Si correo | Todos | `no-reply@example.test` |
| `NOTIFICATIONS_WHATSAPP_CONFIGURED` | Habilita WhatsApp | No | Todos | `false` |
| `NOTIFICATIONS_WHATSAPP_API_URL` | Endpoint proveedor | Si WhatsApp | Todos | `https://api.example.test` |
| `NOTIFICATIONS_WHATSAPP_TOKEN` | Token proveedor | Si WhatsApp | Todos | `<secret>` |
| `NOTIFICATIONS_WORKER_DELAY_MS` | Frecuencia worker | No | Todos | `15000` |
| `NOTIFICATIONS_MAX_ATTEMPTS` | Reintentos | No | Todos | `3` |
| `OTEL_SERVICE_NAME` | Nombre telemetría | No | Todos | `cita-cloud-api` |
| `DEPLOYMENT_ENVIRONMENT` | Etiqueta ambiente | Sí | Todos | `development` |
| `OBSERVABILITY_TRACING_ENABLED` | Activa trazas | No | Todos | `true` |
| `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` | Destino OTLP | Si trazas | Todos | `http://localhost:4318/v1/traces` |
| `OTEL_TRACES_SAMPLER_ARG` | Probabilidad 0..1 | No | Todos | `0.20` |
| `HTTP_SLOW_REQUEST_THRESHOLD_MS` | Umbral HTTP lento | No | Todos | `1000` |
| `DB_SLOW_QUERY_THRESHOLD_MS` | Umbral consulta lenta | No | Todos | `1000` |
| `GRAFANA_ADMIN_USER` | Usuario inicial Grafana | Docker | No prod/local | `admin` |
| `GRAFANA_ADMIN_PASSWORD` | Secreto Grafana | Docker | Todos | `<secret>` |

El usuario SMTP, la contraseña y la dirección `no-reply` se administran como configuración técnica. La pantalla funcional solo permite activar el correo y editar su plantilla. El nombre visible del remitente se obtiene de la clínica autenticada. Configuración e-CF productiva aún no está definida como integración completa.
