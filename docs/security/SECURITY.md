# Seguridad

**Estado:** IMPLEMENTADA PARCIALMENTE  
**Última actualización:** 08/09/2026

## Controles actuales

- Login por empresa + usuario + contraseña; contraseñas BCrypt.
- Sesión gestionada por Spring Security y expirada por inactividad.
- Roles y permisos; controles en navegación y servicios sensibles.
- Aislamiento lógico por `empresa_id`, derivado de la autenticación.
- Claves foráneas compuestas en dominios financieros recientes para impedir relaciones cruzadas.
- Auditoría de operaciones relevantes y métricas de accesos rechazados.
- Errores REST sanitizados; logs correlacionados por `requestId` y `traceId`.
- Descargas y archivos validados contra empresa/permiso por sus servicios.

## Multitenancy

El aislamiento debe aplicarse en vista/controlador, servicio, repositorio, restricciones de base, worker, archivos, reportes, exportaciones y auditoría. Un ID válido de otra empresa debe comportarse como inexistente o prohibido, sin revelar su presencia.

## Secretos y datos sensibles

Contraseñas de base, SMTP, tokens de WhatsApp, claves e-CF y credenciales de producción se suministran por configuración externa o almacenamiento seguro; nunca en Git, logs, Swagger ni Markdown. No usar datos reales de pacientes en pruebas o capturas.

## Integraciones y uploads

- Definir timeout, reintentos limitados y sanitización de errores.
- Validar tipo, tamaño, nombre normalizado y autorización de archivos.
- Evitar rutas construidas libremente por el cliente.
- Los mensajes salientes deben minimizar datos clínicos y respetar la preferencia del paciente.

## Riesgos y controles pendientes

- Rate limiting general no está implementado.
- Sentry no está integrado actualmente.
- Debe formalizarse la rotación y cifrado de secretos por ambiente.
- Se requieren pruebas periódicas de manipulación de UUID, aislamiento de sucursal y archivos.
- HTTPS, cookies seguras, cabeceras del proxy y backups cifrados dependen del despliegue y deben verificarse antes de producción.

Los permisos vigentes se resumen en [PERMISSIONS.md](PERMISSIONS.md). Los incidentes se gestionan con el [Runbook](../operations/RUNBOOK.md).
