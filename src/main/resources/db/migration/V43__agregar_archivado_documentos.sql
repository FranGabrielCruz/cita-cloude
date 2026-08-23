ALTER TABLE documentos ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
ALTER TABLE documentos ADD COLUMN origen VARCHAR(30) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE documentos ADD COLUMN archivado_en TIMESTAMP;
ALTER TABLE documentos ADD COLUMN archivado_por UUID REFERENCES usuarios(id) ON DELETE SET NULL;
ALTER TABLE documentos ADD COLUMN motivo_archivo TEXT;
ALTER TABLE documentos ADD COLUMN actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_documentos_empresa_estado ON documentos(empresa_id, estado, fecha DESC);
