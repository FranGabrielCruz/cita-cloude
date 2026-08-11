CREATE TABLE descansos_horarios_medicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    horario_id UUID NOT NULL REFERENCES horarios_medicos(id) ON DELETE CASCADE,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    descripcion VARCHAR(200),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_descansos_horario ON descansos_horarios_medicos(empresa_id, horario_id);
