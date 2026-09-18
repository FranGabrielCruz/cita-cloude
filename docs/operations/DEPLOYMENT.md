# Despliegue

## Construcción

1. Ejecutar pruebas.
2. Construir el artefacto con Maven/Java 21.
3. Construir la imagen desde `Dockerfile` cuando el destino sea Docker.
4. Inyectar variables/secretos desde el ambiente.

## Orden

1. Respaldar base y archivos.
2. Verificar compatibilidad y espacio.
3. Desplegar una instancia que ejecute Flyway con exclusión mutua operativa.
4. Confirmar `/health/live` y `/health/ready`.
5. Verificar login, métricas, workers, logs y flujo crítico.

## Migraciones

Flyway ejecuta migraciones antes de servir tráfico. Si una falla, no editar una versión aplicada ni forzar `ddl-auto`. Conservar evidencia, detener el despliegue, restaurar desde respaldo si hubo cambios no transaccionales y crear una migración correctiva probada.

## Rollback

El rollback de aplicación usa la imagen anterior solo si su código es compatible con el esquema ya migrado. Las migraciones destructivas requieren estrategia expand/contract; restaurar la base es la última opción y sigue el runbook autorizado.

La guía específica existente de staging está en [DESPLIEGUE-STAGING.md](../DESPLIEGUE-STAGING.md).
