# Reportes financieros y Gestión y Control

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Consultar indicadores financieros y de gestión por rango, filtros disponibles y tenant; exportar resultados a PDF o Excel cuando el permiso lo permita.

## Reglas

- Totales usan exactamente los mismos filtros que el detalle/exportación.
- Facturación, cobros, CxC y caja se presentan como magnitudes distintas.
- Los reportes no mezclan empresas; la sucursal se aplica cuando el reporte la soporta.
- `MENU_REPORTES_FINANCIEROS` controla reportes financieros. Gestión y Control usa `reports.management.view`, `.export.pdf` y `.export.excel`.
- Cada exportación relevante se audita sin incluir datos sensibles en logs.

Pruebas: límites de fecha, tenant, permisos por formato, total-detalle y datos vacíos.
