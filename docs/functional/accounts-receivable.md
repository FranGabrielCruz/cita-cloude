# Cuentas por cobrar

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo

Consultar cargos y saldos pendientes producidos por facturas emitidas, con trazabilidad hasta pagos y aplicaciones.

## Regla financiera

```text
Factura RD$10,000.00
  Pago aplicado RD$4,000.00 -> Cobrado RD$4,000.00
                              Saldo CxC RD$6,000.00
```

`FACTURADO != COBRADO != SALDO POR COBRAR != DINERO EN CAJA`

- El saldo se deriva de cargos y aplicaciones válidas; no de movimientos manuales de caja.
- Una factura pendiente/parcial aparece hasta saldarse o anularse conforme a reglas.
- Filtros y totales siempre aplican empresa y, cuando corresponde, sucursal.
- Exportaciones mantienen los mismos filtros y permisos de la pantalla.

Pruebas: pago parcial, múltiples aplicaciones, anulación/reembolso, tenant y reconciliación de totales.
