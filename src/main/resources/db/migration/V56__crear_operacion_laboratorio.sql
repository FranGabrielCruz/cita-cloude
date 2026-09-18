CREATE TABLE muestras_laboratorio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    orden_id UUID NOT NULL REFERENCES ordenes_estudios(id),
    sucursal_id UUID REFERENCES sucursales(id),
    codigo VARCHAR(40) NOT NULL,
    tipo_muestra VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'RECIBIDA',
    tomada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recibida_por UUID REFERENCES usuarios(id),
    motivo_rechazo TEXT,
    observacion TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_muestra_laboratorio_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT chk_muestra_laboratorio_estado CHECK (estado IN ('RECIBIDA', 'RECHAZADA', 'PROCESADA'))
);

CREATE TABLE muestra_laboratorio_estudios (
    muestra_id UUID NOT NULL REFERENCES muestras_laboratorio(id) ON DELETE CASCADE,
    detalle_orden_id UUID NOT NULL REFERENCES detalle_orden_estudio(id) ON DELETE CASCADE,
    PRIMARY KEY (muestra_id, detalle_orden_id)
);

CREATE TABLE resultados_laboratorio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    orden_id UUID NOT NULL REFERENCES ordenes_estudios(id),
    detalle_orden_id UUID NOT NULL REFERENCES detalle_orden_estudio(id),
    muestra_id UUID REFERENCES muestras_laboratorio(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    resultado TEXT,
    resultado_numerico NUMERIC(18,6),
    unidad VARCHAR(50),
    referencia_minima NUMERIC(18,6),
    referencia_maxima NUMERIC(18,6),
    indicador VARCHAR(20),
    observacion TEXT,
    registrado_por UUID REFERENCES usuarios(id),
    registrado_en TIMESTAMP,
    validado_por UUID REFERENCES usuarios(id),
    validado_en TIMESTAMP,
    motivo_correccion TEXT,
    version INTEGER NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_resultado_laboratorio_estado CHECK (estado IN ('BORRADOR', 'POR_VALIDAR', 'VALIDADO', 'CORREGIDO')),
    CONSTRAINT uq_resultado_laboratorio_detalle_version UNIQUE (detalle_orden_id, version)
);

CREATE INDEX idx_muestras_laboratorio_empresa_orden ON muestras_laboratorio(empresa_id, orden_id, estado);
CREATE INDEX idx_resultados_laboratorio_empresa_orden ON resultados_laboratorio(empresa_id, orden_id, estado);
CREATE INDEX idx_resultados_laboratorio_detalle ON resultados_laboratorio(detalle_orden_id, version DESC);
