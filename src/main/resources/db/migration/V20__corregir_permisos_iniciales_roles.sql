DELETE FROM rol_permisos
WHERE permiso_id IN (SELECT id FROM permisos WHERE codigo LIKE 'MENU_%')
  AND rol_id IN (SELECT id FROM roles WHERE UPPER(nombre) <> 'ADMINISTRADOR');
