CREATE TABLE medicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    usuario_id UUID UNIQUE REFERENCES usuarios(id) ON DELETE SET NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    exequatur VARCHAR(50),
    telefono VARCHAR(30),
    email VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, codigo)
);

CREATE TABLE medico_especialidades (
    medico_id UUID NOT NULL REFERENCES medicos(id) ON DELETE CASCADE,
    especialidad_id UUID NOT NULL REFERENCES especialidades(id) ON DELETE CASCADE,
    PRIMARY KEY (medico_id, especialidad_id)
);

CREATE INDEX idx_medicos_empresa ON medicos(empresa_id);
