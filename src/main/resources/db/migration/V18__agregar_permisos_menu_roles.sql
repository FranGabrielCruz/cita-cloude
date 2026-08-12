ALTER TABLE roles ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;

INSERT INTO permisos (codigo, nombre, descripcion) VALUES
    ('MENU_DASHBOARD', 'Dashboard', 'Permite ver el Dashboard'),
    ('MENU_CITAS', 'Citas', 'Permite ver el m\u00f3dulo de Citas'),
    ('MENU_PACIENTES', 'Pacientes', 'Permite ver el m\u00f3dulo de Pacientes'),
    ('MENU_MEDICOS', 'M\u00e9dicos', 'Permite ver el m\u00f3dulo de M\u00e9dicos'),
    ('MENU_ESPECIALIDADES', 'Especialidades', 'Permite ver el m\u00f3dulo de Especialidades'),
    ('MENU_HORARIOS', 'Horarios', 'Permite ver el m\u00f3dulo de Horarios'),
    ('MENU_CONSULTORIOS', 'Consultorios', 'Permite ver el m\u00f3dulo de Consultorios'),
    ('MENU_SEGUROS', 'Seguros', 'Permite ver el m\u00f3dulo de Seguros'),
    ('MENU_USUARIOS', 'Usuarios', 'Permite ver el m\u00f3dulo de Usuarios'),
    ('MENU_ROLES', 'Roles', 'Permite ver el m\u00f3dulo de Roles'),
    ('MENU_CONFIGURACION', 'Configuraci\u00f3n', 'Permite ver Configuraci\u00f3n')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT roles.id, permisos.id FROM roles CROSS JOIN permisos
ON CONFLICT DO NOTHING;
