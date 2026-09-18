# Cierre de caja

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance y flujo

El cierre compara efectivo esperado y contado, calcula diferencia, registra responsable/motivo, cierra el turno y genera PDF.

`Turno abierto -> movimientos -> arqueo -> diferencia -> autorización/motivo -> cierre -> reporte`

## Reglas

- Un turno cerrado no admite nuevos movimientos ni una segunda modificación del cierre.
- Una diferencia requiere motivo y, cuando corresponde, `CASH_CLOSE_WITH_DIFFERENCE`.
- `CASH_PRINT_CLOSE` controla el PDF; `CASH_VIEW_HISTORY`/`CASH_VIEW_ALL` controlan histórico.
- El reporte muestra tipos y métodos en español, montos firmados, caja, sucursal, turno y responsable.
- El cierre no recalcula ni modifica facturas/pagos históricos.

Pruebas: doble cierre, diferencia sin permiso, movimiento posterior, tenant y conciliación del reporte.
