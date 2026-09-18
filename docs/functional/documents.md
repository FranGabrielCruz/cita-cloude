# Documentos

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo

Guardar, consultar, visualizar y archivar documentos asociados a la operación clínica, respetando tenant, permisos y almacenamiento configurado.

## Reglas

- El nombre físico no se toma directamente del usuario; se normaliza y resuelve dentro de la ruta autorizada.
- La descarga valida empresa, usuario y permiso antes de devolver bytes.
- Se conserva metadato de nombre, tipo, asociación, creador y estado de archivado.
- Archivar no equivale a borrar el archivo histórico.
- No se admiten secretos ni rutas locales en respuestas públicas.

## Manejo de errores

Archivo inexistente o ajeno al tenant se trata como no encontrado/prohibido sin revelar su ubicación. Fallos de almacenamiento se correlacionan por `requestId` y alimentan métricas técnicas.

Pruebas prioritarias: path traversal, UUID de otra empresa, archivo ausente, tipo/tamaño permitido y archivado.
