# Convenciones de API

**Estado:** IMPLEMENTADA PARCIALMENTE  
**Última actualización:** 08/09/2026

El contrato vivo se genera en `/v3/api-docs`; Swagger UI está en `/swagger-ui.html`. Los endpoints actuales no están globalmente versionados: cualquier nueva API pública deberá usar `/api/v1` o documentar la excepción mediante ADR.

## Autenticación y autorización

- Autenticación por sesión segura `JSESSIONID`.
- La empresa se obtiene de la sesión, no de parámetros libres.
- Una ruta puede requerir rol o permiso funcional adicional.
- `401` indica sesión ausente/expirada; `403`, sesión válida sin autorización.

## Datos

- Identificadores: UUID.
- Fechas de contrato: ISO-8601; la UI las presenta según estándares globales.
- Moneda: código ISO 4217 y decimales `BigDecimal`/`NUMERIC`, nunca `double` para cálculos financieros.
- JSON usa nombres definidos por los DTO/modelos actuales. Nuevos contratos deben usar DTO y no exponer entidades JPA.

## Listados

Los endpoints nuevos deben aceptar `page` desde 0, `size` predeterminado 10 con máximo 100, `sort`, filtros explícitos y responder contenido más total/página. Los endpoints históricos sin paginación se consideran deuda compatible.

## Idempotencia y concurrencia

Operaciones financieras repetibles usan una clave de idempotencia dentro del tenant. Una modificación optimista conflictiva responde `409 CONCURRENT_MODIFICATION`. No se debe reintentar ciegamente una operación cuyo resultado sea incierto.

## Estados HTTP

- `200`: lectura o acción completada.
- `201`: recurso creado (objetivo para endpoints nuevos).
- `204`: acción sin cuerpo.
- `400`: formato o validación inválida.
- `401`/`403`: autenticación/autorización.
- `404`: recurso no encontrado.
- `409`: duplicidad, concurrencia o conflicto de estado.
- `422`: regla de negocio no procesable cuando el contrato lo distinga.
- `500`: error no controlado, siempre con `requestId`.

## Errores

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Algunos campos contienen errores.",
    "details": null
  },
  "requestId": "req_example",
  "timestamp": "2026-09-08T20:00:00Z"
}
```

El frontend decide por `error.code`, nunca por el texto de `message`. Consulte el [catálogo](ERROR-CATALOG.md).
