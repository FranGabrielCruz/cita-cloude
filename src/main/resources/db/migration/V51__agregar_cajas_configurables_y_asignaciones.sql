CREATE TABLE cajas (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
 sucursal_id UUID NOT NULL REFERENCES sucursales(id),
 codigo VARCHAR(40) NOT NULL,
 nombre VARCHAR(120) NOT NULL,
 descripcion VARCHAR(500),
 activa BOOLEAN NOT NULL DEFAULT TRUE,
 creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uq_caja_empresa_codigo UNIQUE (empresa_id,codigo)
);
CREATE TABLE cajas_usuarios (
 caja_id UUID NOT NULL REFERENCES cajas(id) ON DELETE CASCADE,
 usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
 PRIMARY KEY (caja_id,usuario_id)
);
ALTER TABLE sesiones_caja ADD COLUMN caja_id UUID REFERENCES cajas(id);
ALTER TABLE movimientos_caja ADD COLUMN caja_id UUID REFERENCES cajas(id);
CREATE INDEX idx_cajas_empresa_sucursal ON cajas(empresa_id,sucursal_id);
CREATE INDEX idx_cajas_usuarios_usuario ON cajas_usuarios(usuario_id);
CREATE INDEX idx_sesiones_caja_caja ON sesiones_caja(empresa_id,caja_id,estado);
INSERT INTO permisos (codigo,nombre,descripcion) VALUES
 ('CASH_REGISTER_VIEW','Ver cajas','Consultar cajas configuradas'),
 ('CASH_REGISTER_CREATE','Crear cajas','Crear cajas configuradas'),
 ('CASH_REGISTER_EDIT','Editar cajas','Editar cajas configuradas'),
 ('CASH_REGISTER_ASSIGN','Asignar usuarios a cajas','Administrar asignaciones de cajas'),
 ('CASH_REGISTER_TOGGLE','Activar o desactivar cajas','Cambiar estado de cajas')
 ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id,permiso_id) SELECT r.id,p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre='ADMINISTRADOR' AND p.codigo LIKE 'CASH_REGISTER_%' ON CONFLICT DO NOTHING;
