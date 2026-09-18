# Laboratorio

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Alcance

Administrar muestras, estudios asociados, resultados versionados, archivos de resultados y finalización del proceso de laboratorio.

## Flujo

`Orden -> Muestra -> En proceso -> Resultado borrador -> Finalizar -> Completado`

Los nombres exactos de estado se toman del modelo/migraciones vigentes y se presentan traducidos en UI.

## Reglas

- Orden, paciente, muestra, resultado y archivo pertenecen al mismo tenant.
- Un resultado final conserva versión e histórico; no se sobrescribe silenciosamente.
- Solo transiciones permitidas habilitan completar o adjuntar resultados.
- La descarga de archivos exige autorización y no expone rutas físicas.
- La información es clínica sensible y debe excluirse de logs generales.

Pruebas: transición inválida, versionado, concurrencia, archivo de otra empresa y resultado sin muestra/orden válida.
