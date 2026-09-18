# Facturación

**Versión:** 1.1 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Crear y editar borradores, emitir o dejar pendiente, consultar detalle/historial, anular, imprimir PDF y abrir el registro de pago. La factura admite servicios y productos con snapshots de código, descripción, precio, descuento e impuesto.

## Estados

`BORRADOR -> PENDIENTE -> PARCIAL -> PAGADA`; una factura elegible puede pasar a `ANULADA`. Las transiciones reales están validadas por el servicio y la base.

## Reglas críticas

- Emitir requiere una caja seleccionada para esa máquina y un turno abierto; la factura conserva `caja_id` y `sesion_caja_id`.
- Si usa comprobante fiscal, consume atómicamente una secuencia activa dentro del rango configurado para la empresa.
- Número de factura, comprobante y clave de idempotencia son únicos dentro del tenant.
- Solo un borrador se edita libremente. Una emitida mantiene snapshots e historial.
- Anular exige permiso y condiciones financieras válidas; no equivale a borrar.
- Emitir y registrar cobro coordina factura, pago, aplicación y caja en una operación controlada. Emitir pendiente crea saldo por cobrar.

Permisos `BILLING_*` y `BILLING_FISCAL_CONFIG`. El PDF incluye clínica, caja y turno. Pruebas: doble emisión, rango fiscal agotado, caja cerrada, totales, descuento, tenant, pago parcial y anulación.
