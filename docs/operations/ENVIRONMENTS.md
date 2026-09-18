# Ambientes

| Ambiente | Propósito | Datos | Integraciones | Observabilidad |
|---|---|---|---|---|
| Development | Desarrollo individual | Ficticios | Sandbox/desactivadas | Muestreo alto, consola/local |
| Testing | Pruebas automáticas | Generados | Dobles/sandbox | Evidencia de test |
| Staging | Validación previa | Anonimizados o ficticios | Sandbox | Métricas, trazas y alertas de prueba |
| Production | Operación clínica | Reales y protegidos | Credenciales productivas | Alertas, retención y acceso restringido |

Cada ambiente usa base, almacenamiento y secretos independientes. No se reutilizan credenciales productivas en desarrollo. `DEPLOYMENT_ENVIRONMENT` etiqueta telemetría; `SPRING_PROFILES_ACTIVE` controla configuración Spring. Producción requiere HTTPS, backups probados, rotación de secretos y acceso mínimo.
