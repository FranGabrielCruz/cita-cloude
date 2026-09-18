# Documentación de Cita Cloud

Este directorio contiene la documentación oficial versionada con el código. Los documentos describen el comportamiento implementado; una propuesta debe identificarse como **BORRADOR**.

## Navegación

- [Funcional](functional/README.md): módulos y reglas de negocio.
- [Estándares globales](functional/GLOBAL-STANDARDS.md): interfaz y comportamiento transversal.
- [Arquitectura](architecture/ARCHITECTURE.md): componentes, capas y decisiones.
- [API](api/API-CONVENTIONS.md): contrato HTTP y [catálogo de errores](api/ERROR-CATALOG.md).
- [Base de datos](database/DATABASE.md): persistencia, migraciones y ERD.
- [Seguridad](security/SECURITY.md): controles y [permisos](security/PERMISSIONS.md).
- [Operaciones](operations/LOCAL-DEVELOPMENT.md): desarrollo, despliegue, observabilidad y recuperación.
- [Manual de usuario](user-guide/getting-started.md): procedimientos para personal de clínicas.

## Fuente de verdad

| Área | Fuente principal |
|---|---|
| Reglas funcionales | Especificación funcional del módulo |
| Contrato HTTP | `/v3/api-docs`, generado desde el código |
| Errores | Código y `api/ERROR-CATALOG.md` |
| Modelo físico | Migraciones Flyway |
| Arquitectura | `ARCHITECTURE.md` y ADR |
| Permisos | Migraciones/código y `PERMISSIONS.md` |
| Operación | Runbooks |
| Cambios por versión | `CHANGELOG.md` |
| Uso del sistema | Manual de usuario |

## Responsabilidades

| Tipo | Responsable |
|---|---|
| Funcional | Producto y Desarrollo |
| Arquitectura | Líder técnico |
| API y base de datos | Backend |
| Seguridad y operaciones | Equipo técnico |
| Manual de usuario | Producto y Soporte |
| Changelog | Desarrollo / Release |

En una etapa temprana una persona puede asumir varios roles.

## Flujo y Definition of Done

`Especificación -> Desarrollo -> Pruebas -> Documentación actualizada -> Terminado`

- [ ] Funcionalidad y validaciones implementadas.
- [ ] Pruebas unitarias, integración, seguridad y E2E aplicables.
- [ ] Migración nueva cuando cambia el esquema; nunca se edita una aplicada.
- [ ] OpenAPI, especificación, permisos, runbook y changelog actualizados cuando aplica.
- [ ] Observabilidad incorporada cuando aplica.
- [ ] Sin secretos, datos clínicos reales ni información financiera sensible en Git.

No se documentan getters, setters, métodos triviales ni código evidente. Se priorizan propósito, reglas, contratos, decisiones, flujos y operación.
