CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO empresas (codigo, nombre, activa)
VALUES ('SUPERADMIN', 'SuperAdmin', TRUE)
ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre, activa = TRUE;

INSERT INTO sucursales (empresa_id, codigo, nombre, activa)
SELECT id, 'PRINCIPAL', 'Sucursal principal', TRUE
FROM empresas WHERE codigo = 'SUPERADMIN'
ON CONFLICT (empresa_id, codigo) DO NOTHING;

INSERT INTO roles (empresa_id, nombre, descripcion, activo)
SELECT id, 'SUPERADMIN', 'Administraci\u00f3n global de empresas', TRUE
FROM empresas WHERE codigo = 'SUPERADMIN'
ON CONFLICT (empresa_id, nombre) DO UPDATE SET activo = TRUE;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT rol.id, permiso.id
FROM roles rol
JOIN empresas empresa ON empresa.id = rol.empresa_id AND empresa.codigo = 'SUPERADMIN'
CROSS JOIN permisos permiso
WHERE rol.nombre = 'SUPERADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO usuarios (empresa_id, usuario, password_hash, nombre, apellido, activo)
SELECT empresa.id, 'superadmin', crypt('superadmin123', gen_salt('bf', 10)), 'Super', 'Administrador', TRUE
FROM empresas empresa
WHERE empresa.codigo = 'SUPERADMIN'
ON CONFLICT (empresa_id, usuario) DO UPDATE SET activo = TRUE;

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT usuario.id, rol.id
FROM usuarios usuario
JOIN empresas empresa ON empresa.id = usuario.empresa_id AND empresa.codigo = 'SUPERADMIN'
JOIN roles rol ON rol.empresa_id = empresa.id AND rol.nombre = 'SUPERADMIN'
WHERE usuario.usuario = 'superadmin'
ON CONFLICT DO NOTHING;
