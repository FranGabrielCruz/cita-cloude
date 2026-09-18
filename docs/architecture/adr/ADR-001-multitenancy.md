# ADR-001 — Base compartida con aislamiento por empresa

**Estado:** Aceptado  
**Fecha:** 08/09/2026

## Contexto

Cita Cloud atiende múltiples clínicas en una aplicación y una base PostgreSQL.

## Decisión

Usar base y esquema compartidos. Todo dato de negocio incorpora `empresa_id`, obtenido de la sesión. Servicios y repositorios filtran por empresa; relaciones críticas usan restricciones compuestas.

## Alternativas consideradas

- Base por empresa: mayor aislamiento físico, mayor costo operativo.
- Esquema por empresa: migraciones y conexiones más complejas.

## Consecuencias

Simplifica despliegue y migración, pero exige revisar tenant en cada consulta, job, archivo, reporte y caché. Las pruebas de acceso cruzado son obligatorias.
