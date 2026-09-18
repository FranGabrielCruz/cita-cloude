# Permisos

**Estado:** IMPLEMENTADA  
**Última actualización:** 08/09/2026

La fuente ejecutable son las migraciones y validaciones de servicios. `ADMINISTRADOR` recibe los permisos operativos creados por las migraciones; `SUPERADMIN` administra tenants y también se reconoce en operaciones sensibles. Los roles personalizados reciben una selección explícita.

## Acceso por módulo

Los códigos `MENU_*` gobiernan visibilidad de Dashboard, agenda, citas, pacientes, médicos, especialidades, horarios, consultorios, seguros, usuarios, roles, configuración, operación clínica, documentos, facturación, pagos, caja, inventario, laboratorio, reportes, gestión y auditoría.

## Permisos de acción especializados

| Recurso | Permisos implementados |
|---|---|
| Auditoría | `AUDIT_VIEW`, `AUDIT_VIEW_DETAILS`, `AUDIT_VIEW_SENSITIVE`, `AUDIT_EXPORT` |
| Servicios | `SERVICES_VIEW`, `SERVICES_CREATE`, `SERVICES_EDIT`, `SERVICES_STATUS` |
| Pagos | `PAYMENTS_CREATE`, `PAYMENTS_VIEW`, `PAYMENTS_VOID`, `PAYMENTS_REFUND` |
| Caja/turno | `CASH_VIEW`, `CASH_OPEN`, `CASH_CREATE_INCOME`, `CASH_CREATE_EXPENSE`, `CASH_CLOSE`, `CASH_CLOSE_WITH_DIFFERENCE`, `CASH_REVERSE_MOVEMENT`, `CASH_VIEW_HISTORY`, `CASH_PRINT_CLOSE`, `CASH_VIEW_ALL` |
| Configuración de cajas | `CASH_REGISTER_VIEW`, `CASH_REGISTER_CREATE`, `CASH_REGISTER_EDIT`, `CASH_REGISTER_ASSIGN`, `CASH_REGISTER_TOGGLE` |
| Inventario | `INVENTORY_VIEW`, `INVENTORY_CREATE`, `INVENTORY_EDIT`, `INVENTORY_MOVEMENT` |
| Facturación | `BILLING_VIEW`, `BILLING_CREATE`, `BILLING_EDIT_DRAFT`, `BILLING_ISSUE`, `BILLING_REGISTER_PAYMENT`, `BILLING_APPLY_DISCOUNT`, `BILLING_CHANGE_PRICE`, `BILLING_VOID`, `BILLING_PRINT`, `BILLING_EXPORT`, `BILLING_FISCAL_CONFIG` |
| Gestión y Control | `reports.management.view`, `reports.management.export.pdf`, `reports.management.export.excel` |
| Notificaciones de citas | `APPOINTMENT_NOTIFICATION_SETTINGS_EDIT` o rol administrativo en el servicio actual |

## Matriz orientativa de roles base

| Recurso | Superadmin | Administrador | Médico | Rol personalizado |
|---|:---:|:---:|:---:|:---:|
| Empresas | ✓ | Solo su empresa | — | Según permiso |
| Configuración y roles | ✓ | ✓ | — | Según permiso |
| Mi agenda | Según contexto | ✓ | ✓ | Según permiso |
| Datos clínicos | Según contexto | ✓ | Según permiso | Según permiso |
| Facturación, pagos y caja | Según contexto | ✓ | — | Según permiso específico |
| Auditoría sensible | ✓ | ✓ si asignado | — | Solo permiso explícito |

La UI no sustituye la autorización del backend. Al agregar o cambiar un permiso se actualizan migración, servicio, pruebas y este documento.
