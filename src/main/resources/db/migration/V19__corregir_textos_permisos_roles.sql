UPDATE permisos SET nombre = U&'Configuraci\00F3n', descripcion = U&'Permite ver Configuraci\00F3n'
WHERE codigo = 'MENU_CONFIGURACION';
UPDATE permisos SET nombre = U&'M\00E9dicos', descripcion = U&'Permite ver el m\00F3dulo de M\00E9dicos'
WHERE codigo = 'MENU_MEDICOS';
UPDATE roles SET descripcion = U&'Acceso para m\00E9dicos' WHERE nombre = 'MEDICO';
