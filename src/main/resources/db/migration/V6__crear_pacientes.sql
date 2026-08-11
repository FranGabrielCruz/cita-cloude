CREATE TABLE pacientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    documento VARCHAR(50) NOT NULL,
    tipo_documento VARCHAR(20) DEFAULT 'CEDULA',
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    genero VARCHAR(20),
    telefono VARCHAR(30),
    email VARCHAR(100),
    direccion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, documento)
);

CREATE INDEX idx_pacientes_empresa ON pacientes(empresa_id);
CREATE INDEX idx_pacientes_documento ON pacientes(empresa_id, documento);
