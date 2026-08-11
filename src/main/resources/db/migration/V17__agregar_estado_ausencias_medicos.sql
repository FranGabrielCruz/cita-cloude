ALTER TABLE ausencias_medicos
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_ausencias_empresa_medico_activo
    ON ausencias_medicos(empresa_id, medico_id, activo);
