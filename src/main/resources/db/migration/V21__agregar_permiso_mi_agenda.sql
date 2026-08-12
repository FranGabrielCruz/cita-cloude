INSERT INTO permisos (codigo, nombre, descripcion)
VALUES ('MENU_MI_AGENDA', 'Mi agenda', 'Permite ver la agenda personal del m\u00e9dico')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT roles.id, permisos.id
FROM roles CROSS JOIN permisos
WHERE permisos.codigo = 'MENU_MI_AGENDA'
  AND UPPER(roles.nombre) IN ('ADMINISTRADOR', 'MEDICO')
ON CONFLICT DO NOTHING;
