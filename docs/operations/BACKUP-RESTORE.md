# Backup y restauración

**Estado:** POLÍTICA BASE; frecuencias y proveedores deben aprobarse antes de producción.

## Alcance

- PostgreSQL completo, incluido historial Flyway.
- Directorios de documentos, uploads, resultados y PDF persistentes.
- Configuración no secreta y referencia al gestor de secretos; los secretos se respaldan por su sistema autorizado.

## Política por definir

Propietario, frecuencia, retención, ubicación externa, cifrado, inmutabilidad y alertas deben establecerse por ambiente. No se inventan RPO/RTO contractuales.

## Restauración

1. Autorizar y registrar el incidente.
2. Elegir copia y punto temporal; validar checksum/cifrado.
3. Crear destino aislado y compatible.
4. Restaurar base y archivos correspondientes.
5. Ejecutar Flyway solo después de identificar la versión restaurada.
6. Validar tenants, conteos, relaciones, archivos, login y flujos críticos.
7. Documentar duración, pérdida estimada y aprobación de conmutación.

Un job `SUCCESS` no prueba recuperabilidad. Realizar ensayos periódicos y registrar evidencia sin datos sensibles.
