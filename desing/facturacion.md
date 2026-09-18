# Facturación

Referencia funcional y visual documentada el 01/09/2026 a partir de la especificación suministrada para el módulo **Gestión financiera y operativa > Facturación**.

## Jerarquía

1. Título `FACTURACIÓN` y subtítulo `Gestión y seguimiento de facturas`.
2. Acción primaria azul con icono `+` y tooltip `Nueva factura`.
3. Búsqueda y filtros de período, estado, sucursal, paciente y médico.
4. Indicadores: Facturado, Facturas, Pendiente y Pagadas.
5. Tabla adaptable con factura, fecha, paciente, totales, estado financiero, estado e-CF y acciones.
6. Paginación de 10 registros por defecto, con opciones 10, 20, 50 y 100.

## Formulario

- Modal amplio y responsive.
- Cabecera, paciente, detalle, resumen y observaciones claramente separados.
- Los servicios y productos se agregan desde catálogos existentes.
- Guardar usa icono verde; cancelar usa icono gris; ambas acciones tienen tooltip.
- Los montos se presentan con separador de miles y dos decimales.
- La caja asignada y la sesión abierta se muestran en modo de solo lectura; sin esa vinculación no se puede guardar la factura.
- Los tipos de comprobante se presentan con nombres legibles, sin guiones bajos.
- Emitir abre un diálogo de cobro, registra factura y pago en una sola operación y abre el PDF en una pestaña nueva.

## Configuración fiscal

- Configuración incluye una tarjeta de secuencias fiscales por empresa.
- Cada tipo define nombre, prefijo, número inicial, número final, siguiente número y estado.
- El número fiscal se consume de forma atómica al emitir, nunca al guardar el borrador.
- Una secuencia agotada o inactiva impide la emisión y conserva intactos el borrador y el pago.

## Estados

- Borrador: variante primaria.
- Pendiente/parcial: variante de contraste.
- Pagada: variante de éxito.
- Anulada: variante de error.
- El estado e-CF se presenta separado del estado financiero.

## Responsive y modo oscuro

- Indicadores en cuadrícula adaptable; dos columnas en móvil.
- Filtros con salto de línea y controles de ancho flexible.
- Tabla sin altura vacía fija.
- Fondos, bordes y textos usan variables Lumo para responder al tema claro u oscuro.
