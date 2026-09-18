# Pacientes

**Versión:** 1.0 · **Estado:** IMPLEMENTADA · **Última actualización:** 08/09/2026

## Objetivo y alcance

Registrar y consultar pacientes, datos de contacto, aseguramiento e historial asociado a la clínica autenticada. Incluye el enlace con citas, documentos, antecedentes, alergias, diagnósticos y otras áreas clínicas existentes.

## Reglas

- Todo paciente tiene `empresa_id`; un usuario nunca puede consultar o asociar un paciente de otra empresa.
- Los datos personales se validan y se minimizan en logs, exportaciones y notificaciones.
- El correo/teléfono usado para notificar debe proceder del paciente vigente y respetar preferencias configuradas.
- Aseguradora, plan y seguro del paciente deben ser compatibles con el mismo tenant.
- El historial no debe alterarse al modificar datos demográficos actuales.

## Seguridad y pruebas

El acceso requiere el permiso de menú/rol correspondiente y permisos clínicos cuando aplique. Las descargas clínicas exigen autorización adicional. Pruebas prioritarias: manipulación de UUID, aislamiento entre empresas, búsqueda, validación de contacto y acceso a documentos/historial.

Aplican los [estándares globales](GLOBAL-STANDARDS.md).
