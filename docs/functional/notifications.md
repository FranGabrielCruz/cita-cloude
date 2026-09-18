# Notificaciones

**Versión:** 1.1 · **Estado:** IMPLEMENTADA · **Última actualización:** 10/09/2026

## Alcance

Notificaciones internas y notificaciones automáticas de citas por correo. Incluye preferencias por clínica, plantillas, entregas, outbox, reintentos y estado visible. WhatsApp permanece en la arquitectura para una activación futura, pero está retirado de la configuración funcional.

## Flujo

`Cambio de cita -> evento transaccional -> outbox -> worker -> proveedor -> entrega`

## Reglas

- La creación de la cita no depende de la disponibilidad inmediata del proveedor externo.
- Cada mensaje conserva tenant, canal, destinatario normalizado, referencia y estado.
- Los reintentos son limitados; un error permanente queda diagnosticable sin bloquear indefinidamente la cola.
- SMTP usa host, puerto, usuario, contraseña y dirección remitente definidos por el ambiente. El nombre visible del remitente corresponde a la clínica.
- WhatsApp está temporalmente deshabilitado en la configuración funcional.
- Editar configuración exige rol administrativo o `APPOINTMENT_NOTIFICATION_SETTINGS_EDIT`.

La tabla de notificaciones usa paginación configurable, predeterminada en 10, y no puede quedar oculta por el footer. Pruebas: evento duplicado, tenant, destinatario ausente, retry, proveedor fallido y sanitización.
