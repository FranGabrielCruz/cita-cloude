# Inventario

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Categorías, unidades, productos, existencias, lotes y movimientos de entrada, salida y ajuste.

## Reglas

- Producto, categoría, unidad, lote, existencia y movimiento pertenecen a la misma empresa; las existencias se separan por sucursal cuando aplica.
- Un movimiento es histórico y no se edita como si fuera el saldo actual; una corrección genera un movimiento compensatorio autorizado.
- Cantidad, dirección, motivo y usuario se validan en servicio y se auditan.
- Precio de venta no es costo unitario. Facturación toma un snapshot del precio; una entrada conserva su costo.
- No se permite salida que viole la política de existencia definida por el servicio.
- Permisos: `INVENTORY_VIEW`, `INVENTORY_CREATE`, `INVENTORY_EDIT`, `INVENTORY_MOVEMENT`.

Pruebas: concurrencia de saldo, lote de otro tenant, cantidades inválidas, ajuste y vínculo con detalle de factura.
