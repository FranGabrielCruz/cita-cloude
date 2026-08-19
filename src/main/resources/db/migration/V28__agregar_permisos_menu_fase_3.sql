INSERT INTO permisos (codigo, nombre, descripcion) VALUES
    ('MENU_ECF', 'e-CF', 'Acceder a comprobantes fiscales electrónicos'),
    ('MENU_CAJA', 'Caja', 'Gestionar movimientos de caja'),
    ('MENU_ARS', 'ARS', 'Gestionar aseguradoras ARS'),
    ('MENU_INVENTARIO', 'Inventario', 'Gestionar inventario'),
    ('MENU_FARMACIA', 'Farmacia', 'Gestionar farmacia'),
    ('MENU_LABORATORIO', 'Laboratorio', 'Gestionar laboratorio'),
    ('MENU_REPORTES_FINANCIEROS', 'Reportes financieros', 'Consultar reportes financieros')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR'
  AND p.codigo IN ('MENU_ECF', 'MENU_CAJA', 'MENU_ARS', 'MENU_INVENTARIO', 'MENU_FARMACIA', 'MENU_LABORATORIO', 'MENU_REPORTES_FINANCIEROS')
ON CONFLICT DO NOTHING;
