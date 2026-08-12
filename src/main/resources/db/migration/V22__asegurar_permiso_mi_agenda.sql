INSERT INTO permisos (codigo, nombre, descripcion)
VALUES ('MENU_MI_AGENDA', 'Mi agenda', 'Permite ver la agenda personal del médico')
ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre, descripcion = EXCLUDED.descripcion;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT roles.id, permisos.id
FROM roles
JOIN permisos ON permisos.codigo = 'MENU_MI_AGENDA'
WHERE UPPER(roles.nombre) IN ('ADMINISTRADOR', 'MEDICO')
ON CONFLICT DO NOTHING;
