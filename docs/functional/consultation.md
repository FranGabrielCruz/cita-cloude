# Consulta y expediente clínico

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Recepción/check-in, sala de espera, signos vitales, consulta médica, antecedentes, alergias, diagnósticos, tratamientos, recetas, órdenes de estudios e historial clínico.

## Flujo

```text
Cita -> Check-in -> Sala de espera -> Signos vitales -> Consulta
                                                  |-> Diagnóstico
                                                  |-> Tratamiento/receta
                                                  \-> Orden de estudio/laboratorio
```

## Reglas

- Paciente, cita, médico, consulta y elementos derivados pertenecen a la misma empresa.
- Cada transición valida el estado previo; no se debe duplicar check-in o consulta para el mismo evento cuando el modelo lo impida.
- Recetas y órdenes emitidas conservan numeración y snapshot documental; su PDF no depende de cambios posteriores de catálogo.
- La información clínica es sensible: no se registra contenido médico completo en logs o auditoría general.
- Los permisos `MENU_CHECKIN`, `MENU_SALA_ESPERA`, `MENU_SIGNOS_VITALES`, `MENU_CONSULTA_MEDICA`, `MENU_EXPEDIENTE_CLINICO` y permisos de submódulo gobiernan el acceso.

Pruebas prioritarias: estado inválido, acceso cruzado, asociación paciente/cita, PDF autorizado y descarga de resultados.
