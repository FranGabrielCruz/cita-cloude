# Catálogo de errores

**Estado:** IMPLEMENTADA  
**Última actualización:** 08/09/2026

| Código | HTTP | Significado / acción |
|---|---:|---|
| `VALIDATION_ERROR` | 400 | Uno o más campos no cumplen validaciones; revisar `details`. |
| `INVALID_REQUEST` | 400 | JSON, parámetro o formato inválido. |
| `UNAUTHORIZED` | 401 | Iniciar sesión nuevamente. |
| `FORBIDDEN` | 403 | El usuario no posee el permiso requerido. |
| `NOT_FOUND` | 404 | Recurso del tenant no encontrado. |
| `PATIENT_NOT_FOUND` | 404 | Paciente no encontrado dentro del tenant. |
| `DOCTOR_NOT_FOUND` | 404 | Médico no encontrado dentro del tenant. |
| `INVOICE_NOT_FOUND` | 404 | Factura no encontrada dentro del tenant. |
| `DUPLICATE_RESOURCE` | 409 | Una restricción única impide duplicar el recurso. |
| `CONCURRENT_MODIFICATION` | 409 | Otro usuario modificó el registro; recargar antes de reintentar. |
| `CONFLICT` | 409 | Estado o regla incompatible con la acción solicitada. |
| `INVOICE_ALREADY_ISSUED` | 409 | La factura/origen ya fue emitido o no puede editarse como borrador. |
| `BUSINESS_RULE_ERROR` | 422 | Regla de negocio incumplida cuando se usa `BusinessRuleException`. |
| `INVALID_INVOICE_STATE` | 422 | El estado actual no permite la operación de facturación. |
| `INVALID_INVOICE_ITEM` | 422 | Falta una línea válida de servicio o producto. |
| `INVALID_INVOICE_AMOUNT` | 422 | Cantidad, precio, descuento, impuesto o total inválido. |
| `SERVICE_NOT_BILLABLE` | 422 | Servicio inactivo/no facturable. |
| `PRODUCT_NOT_BILLABLE` | 422 | Producto no disponible para facturar. |
| `PRODUCT_INACTIVE` | 422 | Producto inactivo. |
| `INVOICE_HAS_PAYMENTS` | 422 | La factura tiene pagos que impiden anularla. |
| `INVOICE_VOID_NOT_ALLOWED` | 422 | La factura o su estado fiscal no permiten anulación directa. |
| `REQUEST_ERROR` | variable | Error HTTP controlado no clasificado todavía. |
| `INTERNAL_SERVER_ERROR` | 500 | Fallo inesperado; entregar `requestId` a soporte. |

Las subclases de `ApplicationException` pueden aportar códigos más específicos. Un código nuevo debe ser estable, documentarse aquí y probarse. Nunca se exponen SQL, stack traces, credenciales ni detalles de proveedores en la respuesta pública.
