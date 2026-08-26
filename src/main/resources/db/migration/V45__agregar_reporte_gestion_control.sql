INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('reports.management.view', 'Gestión y Control', 'Consultar el reporte de gestión y control'),
 ('reports.management.export.pdf', 'Exportar Gestión y Control PDF', 'Exportar el reporte de gestión y control en PDF'),
 ('reports.management.export.excel', 'Exportar Gestión y Control Excel', 'Exportar el reporte de gestión y control en Excel'),
 ('MENU_GESTION_CONTROL', 'Gestión y Control', 'Mostrar Gestión y Control en el menú')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMINISTRADOR','SUPERADMIN') AND p.codigo IN ('reports.management.view','reports.management.export.pdf','reports.management.export.excel','MENU_GESTION_CONTROL')
ON CONFLICT DO NOTHING;
