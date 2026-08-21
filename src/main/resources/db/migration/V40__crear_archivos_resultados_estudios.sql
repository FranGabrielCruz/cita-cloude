CREATE TABLE archivos_resultados_estudios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    paciente_id UUID NOT NULL REFERENCES pacientes(id),
    orden_id UUID NOT NULL REFERENCES ordenes_estudios(id) ON DELETE CASCADE,
    detalle_orden_id UUID NOT NULL REFERENCES detalle_orden_estudio(id) ON DELETE CASCADE,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    nombre_original VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamanio_bytes BIGINT NOT NULL,
    checksum VARCHAR(64),
    subido_por UUID NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_archivos_resultados_detalle ON archivos_resultados_estudios(empresa_id, detalle_orden_id);
