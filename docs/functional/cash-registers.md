# Cajas y turnos

**Versión:** 1.1 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Configurar cajas por empresa/sucursal, asignar usuarios y abrir un turno con fondo inicial. La máquina recuerda la caja seleccionada en el almacenamiento del navegador, pero el backend valida cada operación.

## Reglas

- Caja, sucursal, usuario y turno pertenecen al mismo tenant.
- Solo una caja activa y autorizada puede seleccionarse.
- Facturación no emite si la caja no está configurada o su turno está cerrado.
- Abrir, ingresar, egresar y cerrar requieren permisos `CASH_*`; administrar cajas requiere `CASH_REGISTER_*`.
- Los movimientos poseen número, método, dirección, referencia e idempotencia. Los tipos técnicos se traducen al español en pantalla/PDF.
- La selección en navegador no concede autorización ni sustituye la consulta del turno vigente.

Pruebas: caja ajena, usuario no asignado, turno duplicado, selección obsoleta, idempotencia y cambio de sucursal.
