CREATE TABLE ordenes_estudios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    paciente_id UUID NOT NULL REFERENCES pacientes(id),
    diagnostico_id UUID REFERENCES diagnosticos(id),
    creado_por UUID REFERENCES usuarios(id),
    numero VARCHAR(30) UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    prioridad VARCHAR(20) NOT NULL,
    indicaciones_clinicas TEXT,
    preparacion_instrucciones TEXT,
    observaciones TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    emitido_en TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ordenes_estudios_empresa_paciente ON ordenes_estudios(empresa_id, paciente_id, creado_en DESC);
CREATE TABLE detalle_orden_estudio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES ordenes_estudios(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    estudio VARCHAR(200) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_resultado DATE,
    resultado TEXT,
    observaciones_resultado TEXT,
    centro_realizador VARCHAR(200),
    archivo_url VARCHAR(500),
    registrado_por UUID REFERENCES usuarios(id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_detalle_orden_estudio_orden ON detalle_orden_estudio(orden_id);
CREATE TABLE contador_ordenes_estudios (
    empresa_id UUID PRIMARY KEY REFERENCES empresas(id) ON DELETE CASCADE,
    ultimo_numero INTEGER NOT NULL DEFAULT 0
);
