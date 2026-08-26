-- La auditoría es un registro append-only: las correcciones se representan como nuevos eventos.
ALTER TABLE auditoria
    ADD COLUMN IF NOT EXISTS resultado VARCHAR(12) NOT NULL DEFAULT 'SUCCESS',
    ADD COLUMN IF NOT EXISTS recurso VARCHAR(255), ADD COLUMN IF NOT EXISTS paciente_id UUID,
    ADD COLUMN IF NOT EXISTS rol_usuario VARCHAR(100), ADD COLUMN IF NOT EXISTS dispositivo VARCHAR(255),
    ADD COLUMN IF NOT EXISTS motivo TEXT, ADD COLUMN IF NOT EXISTS sensible BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE auditoria ADD CONSTRAINT chk_auditoria_resultado CHECK (resultado IN ('SUCCESS', 'FAILED', 'DENIED'));
CREATE INDEX IF NOT EXISTS idx_auditoria_empresa_resultado_fecha ON auditoria(empresa_id, resultado, fecha DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_empresa_usuario_fecha ON auditoria(empresa_id, usuario_id, fecha DESC);
CREATE OR REPLACE FUNCTION impedir_modificacion_auditoria() RETURNS TRIGGER AS $$ BEGIN RAISE EXCEPTION 'Los eventos de auditoría son inmutables'; END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_auditoria_inmutable ON auditoria;
CREATE TRIGGER trg_auditoria_inmutable BEFORE UPDATE OR DELETE ON auditoria FOR EACH ROW EXECUTE FUNCTION impedir_modificacion_auditoria();
INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('AUDIT_VIEW','Ver auditoría','Consultar eventos de auditoría de la empresa'), ('AUDIT_VIEW_DETAILS','Ver detalle de auditoría','Consultar detalle y cambios de un evento'),
 ('AUDIT_VIEW_SENSITIVE','Ver auditoría sensible','Consultar eventos de acceso sensible'), ('AUDIT_EXPORT','Exportar auditoría','Exportar eventos de auditoría') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id, permiso_id) SELECT r.id,p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre IN ('ADMINISTRADOR','SUPERADMIN') AND p.codigo IN ('AUDIT_VIEW','AUDIT_VIEW_DETAILS','AUDIT_VIEW_SENSITIVE','AUDIT_EXPORT') ON CONFLICT DO NOTHING;
