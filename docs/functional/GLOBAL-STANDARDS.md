# Estándares globales funcionales

**Estado:** IMPLEMENTADA  
**Última actualización:** 08/09/2026

## Interfaz

- Diseño Vaadin moderno, claro, responsive y compatible con dark mode.
- Tablas con paginación predeterminada de 10 y opciones 10, 20, 50 y 100; mostrar el rango y total cuando el componente lo permita.
- La altura del contenido debe ajustarse sin quedar oculta por el footer.
- Fechas visibles `DD/MM/YYYY`; fecha y hora `DD/MM/YYYY hh:mm AM/PM`.
- Montos con dos decimales y separadores, conservando la moneda, por ejemplo `RD$ 1,000.00`.
- Estados mediante badges legibles en español; no mostrar nombres técnicos con guion bajo.
- Carga con indicador o skeleton, estado vacío explicativo y errores mediante toast comprensible.

## Botones

- Nuevo: azul, icono `+` y tooltip contextual.
- Guardar o confirmar: verde, icono blanco y tooltip descriptivo.
- Cancelar o cerrar: gris, icono negro y tooltip “Cancelar” o “Cerrar”.
- Eliminar/anular: rojo cuando la acción sea destructiva y confirmación previa.
- Acciones de tabla: iconos alineados, sin texto cuando el significado sea claro y siempre con tooltip.
- En diálogos operativos se usa una huella visual uniforme de 40 × 40 px para botones de solo icono.

## Comportamiento transversal

- Confirmar operaciones irreversibles e indicar su consecuencia.
- Validar en UI para ayudar al usuario y repetir toda regla relevante en servicios/backend.
- Derivar empresa y usuario de la sesión; nunca aceptar `empresa_id` como autorización.
- Filtrar sucursal cuando el módulo la utilice y validar asociaciones entre empresa, sucursal y recursos.
- Ocultar o bloquear acciones según rol y permiso; el backend sigue siendo la autoridad.
- Auditar operaciones sensibles, financieras, clínicas y administrativas sin registrar secretos.
- No mostrar stack traces al usuario. Los errores técnicos deben incluir `requestId` para soporte.
