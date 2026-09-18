# Servicios

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo

Mantener el catálogo de prestaciones facturables de cada clínica, con código, descripción, precio de venta, impuesto y estado.

## Reglas

- Código y recurso pertenecen a la empresa autenticada.
- Solo servicios activos/disponibles pueden seleccionarse para nuevas operaciones.
- Precio e impuesto del catálogo son valores sugeridos; al facturar se copian como snapshot al detalle.
- Modificar el catálogo no cambia facturas históricas.
- Acciones: `SERVICES_VIEW`, `SERVICES_CREATE`, `SERVICES_EDIT`, `SERVICES_STATUS`.

Pruebas: duplicidad por empresa, activación/desactivación, aislamiento y snapshot de factura.
