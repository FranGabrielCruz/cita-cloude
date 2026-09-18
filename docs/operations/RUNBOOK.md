# Runbook

Antes de actuar: registrar hora/ambiente, preservar `requestId`/`traceId`, evitar cambios destructivos y escalar cuando exista riesgo de pérdida, seguridad o impacto clínico. Tras recuperar, verificar el flujo y documentar causa.

## RB-001 API no disponible

**Síntomas:** health falla o conexión rechazada.  
**Verificar:** proceso/contenedor, puerto, `/health/live`, logs, memoria/disco y DB.  
**Recuperación:** corregir configuración; reiniciar una instancia de forma controlada.  
**Verificación:** readiness, login y solicitud representativa. **Escalar:** reinicios repetidos o datos en riesgo.

## RB-002 Base de datos no disponible

**Verificar:** conectividad, credenciales, PostgreSQL, conexiones, espacio y logs.  
**Recuperación:** restablecer servicio/conectividad; no ejecutar DDL manual.  
**Verificación:** readiness y lectura/escritura ficticia. **Escalar:** corrupción o restauración requerida.

## RB-003 API lenta / RB-004 CPU / RB-005 Memoria / RB-006 Disco

**Verificar:** Grafana/Prometheus por servicio, endpoint, JVM y contenedor; consultas lentas; cola; almacenamiento/logs.  
**Recuperación:** limitar carga problemática, liberar solo archivos identificados por política, escalar recursos o corregir consulta.  
**Verificación:** latencia/uso regresan a normal. **Escalar:** tendencia sostenida o capacidad crítica.

## RB-007 Worker detenido / RB-008 cola acumulada

**Verificar:** proceso Spring, scheduler, tamaño/edad del outbox, errores, proveedor y reintentos.  
**Recuperación:** resolver dependencia y reactivar worker; no duplicar filas manualmente.  
**Verificación:** pendientes disminuyen y entregas no se duplican. **Escalar:** mensajes estancados o poison message.

## RB-009 Correo no envía

**Verificar:** habilitación de clínica, remitente/usuario, host, puerto, TLS, secreto, destinatario, outbox y respuesta SMTP.  
**Recuperación:** corregir configuración autorizada y reintentar desde el mecanismo previsto.  
**Verificación:** entrega de prueba y métricas. **Escalar:** bloqueo/reputación del proveedor.

## RB-010 WhatsApp no envía

**Verificar:** canal, URL, token, número, referencia/idioma/variables de plantilla, outbox y respuesta del proveedor.  
**Recuperación:** corregir configuración/plantilla aprobada y reintentar controladamente.  
**Verificación:** entrega de prueba. **Escalar:** rechazo persistente del proveedor.

## RB-011 e-CF falla

**Verificar:** estado interno, secuencia/rango, caja/turno y logs. La integración externa no está marcada completa.  
**Recuperación:** no inventar números ni reenviar sin idempotencia; seguir procedimiento del proveedor cuando se implemente.  
**Escalar:** numeración consumida con resultado incierto o rechazo fiscal.

## RB-012 PDF falla

**Verificar:** ID/tenant, plantilla, datos requeridos, fuentes, almacenamiento, memoria y métrica PDF.  
**Recuperación:** corregir dato/plantilla y regenerar sin cambiar el documento financiero.  
**Verificación:** abrir PDF y revisar contenido. **Escalar:** falla masiva o documento inconsistente.

## RB-013 Restauración de backup

Declarar incidente, aislar destino, seleccionar punto autorizado, verificar integridad, restaurar DB y archivos en ambiente aislado, ejecutar validaciones y obtener aprobación antes de conmutar. Seguir [BACKUP-RESTORE.md](BACKUP-RESTORE.md).
