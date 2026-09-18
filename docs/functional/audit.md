# Auditoría

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo

Responder quién realizó una acción, cuándo, sobre qué recurso, desde qué contexto y con qué resultado. No sustituye logs técnicos u observabilidad.

## Reglas

- Cada evento incluye empresa y, cuando existe, usuario; nunca mezcla tenants.
- Datos anteriores/nuevos registran cambios necesarios, no contraseñas, tokens ni contenido clínico completo.
- Eventos sensibles requieren `AUDIT_VIEW_SENSITIVE`; detalle y exportación tienen permisos separados.
- La auditoría es append-only desde el flujo normal y no se edita desde la UI.
- Búsqueda y exportación conservan filtros y autorización.

Permisos: `AUDIT_VIEW`, `AUDIT_VIEW_DETAILS`, `AUDIT_VIEW_SENSITIVE`, `AUDIT_EXPORT`. Pruebas: tenant, ocultación sensible, evento de operación fallida y exportación autorizada.
