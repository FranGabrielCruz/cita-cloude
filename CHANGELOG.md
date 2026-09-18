# Changelog

Los cambios relevantes de Cita Cloud se registran aquí siguiendo las categorías Added, Changed, Fixed y Security. Las versiones publicadas deben usar fecha `YYYY-MM-DD`.

## Unreleased

### Added

- Estructura oficial de documentación técnica, funcional, operativa y de usuario.
- Contrato OpenAPI generado y Swagger UI para los controladores HTTP implementados.
- Entorno de observabilidad para ejecutar Cita Cloud desde IntelliJ.

### Changed

- Facturación vinculada a caja y turno, con secuencias de comprobantes fiscales configurables.
- Notificaciones de citas por correo y WhatsApp mediante outbox y configuración por clínica.

### Fixed

- Métricas técnicas disponibles para autenticación, pagos, notificaciones, PDF y almacenamiento.

### Security

- Respuestas de error HTTP normalizadas con `requestId` y sin exponer detalles internos.
