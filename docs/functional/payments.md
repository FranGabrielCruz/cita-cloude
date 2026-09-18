# Pagos

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Registrar, consultar, aplicar, anular, reembolsar e imprimir recibos de pagos.

## Reglas

- Un pago y sus aplicaciones pertenecen a la misma empresa y paciente/cargo compatibles.
- El total aplicado no supera el saldo válido. Pago parcial reduce CxC; no marca la factura pagada hasta completar el total.
- Un pago de caja produce el movimiento correspondiente en un turno abierto.
- Clave de idempotencia evita duplicar cobros ante reintentos.
- Anulación y reembolso son eventos distintos, autorizados y auditados; no eliminan el registro.
- Métodos admitidos se normalizan para mostrar español sin guiones bajos.
- Permisos: `PAYMENTS_CREATE`, `PAYMENTS_VIEW`, `PAYMENTS_VOID`, `PAYMENTS_REFUND`.

Pruebas: pago mayor al saldo, reintento idéntico, concurrencia, caja cerrada, anulación y recibo autorizado.
