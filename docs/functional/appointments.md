# Citas

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo y alcance

Administrar agenda, creación, edición, aprobación, reprogramación y cancelación de citas dentro de la empresa autenticada. Intervienen paciente, médico, especialidad, sucursal, consultorio, tipo, fecha y hora.

## Flujo y reglas

`Paciente -> Especialidad -> Médico -> Sucursal -> Consultorio -> Fecha/hora -> Seguro -> Motivo -> Guardar`

- La cita pertenece a una empresa; todas sus relaciones deben pertenecer al mismo tenant.
- No se permiten solapamientos, horas fuera del horario médico ni períodos de ausencia/descanso.
- Los estados implementados contemplan `PENDIENTE`, `CONFIRMADA`, `EN_ESPERA`, `EN_CONSULTA`, `ATENDIDA`, `CANCELADA`, `REPROGRAMADA` y `NO_ASISTIO` según el flujo que los consume.
- Crear o modificar una cita genera historial/auditoría y puede encolar notificaciones automáticas.
- Los filtros de agenda respetan médico, sucursal, consultorio, estado y rango de fechas.

## Roles, errores y pruebas

El acceso se controla por permisos `MENU_CITAS`, `MENU_MI_AGENDA` y módulos operativos relacionados. Conflictos de disponibilidad deben mostrarse como regla de negocio, sin guardar parcialmente. Pruebas prioritarias: cruce de tenant, concurrencia sobre el mismo horario, ausencias, reprogramación y generación única de eventos de notificación.

Aplican los [estándares globales](GLOBAL-STANDARDS.md).
