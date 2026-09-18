# Recuperación ante desastres

**Estado:** BORRADOR

Escenarios: servidor perdido, base corrupta, proveedor/región indisponible, eliminación accidental, despliegue defectuoso grave y pérdida de almacenamiento.

## Estrategia base

1. Declarar incidente y responsable.
2. Contener escrituras si amenazan consistencia.
3. Evaluar último backup verificado y estado de archivos/secretos.
4. Provisionar infraestructura limpia.
5. Restaurar según el procedimiento probado.
6. Verificar seguridad, migraciones, tenants, integraciones y observabilidad.
7. Conmutar con aprobación y comunicar impacto.
8. Realizar análisis posterior y acciones preventivas.

RPO y RTO permanecen **por definir** hasta medir backups, restauraciones y capacidad real. No prometer objetivos que el sistema aún no pueda demostrar.
