CREATE TABLE configuracion_notificaciones_cita (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    evento VARCHAR(50) NOT NULL,
    correo_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    asunto_correo TEXT NOT NULL,
    mensaje_correo TEXT NOT NULL,
    whatsapp_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    mensaje_whatsapp TEXT NOT NULL,
    whatsapp_template_id VARCHAR(150),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por UUID REFERENCES usuarios(id),
    CONSTRAINT uq_config_notificacion_cita_empresa_evento UNIQUE (empresa_id, evento)
);

CREATE TABLE entregas_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    evento VARCHAR(50) NOT NULL,
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    canal VARCHAR(20) NOT NULL,
    destinatario VARCHAR(255),
    destinatario_enmascarado VARCHAR(255),
    asunto TEXT,
    mensaje_renderizado TEXT,
    estado VARCHAR(20) NOT NULL,
    proveedor VARCHAR(80),
    referencia_externa VARCHAR(255),
    cantidad_intentos INTEGER NOT NULL DEFAULT 0,
    codigo_fallo VARCHAR(80),
    motivo_fallo VARCHAR(500),
    creada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviada_en TIMESTAMP,
    actualizada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_entrega_notificacion_idempotente UNIQUE (empresa_id, entidad_id, evento, canal)
);

CREATE TABLE outbox_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    entrega_id UUID NOT NULL UNIQUE REFERENCES entregas_notificacion(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INTEGER NOT NULL DEFAULT 0,
    proximo_intento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    procesado_en TIMESTAMP
);

CREATE INDEX idx_entregas_notificacion_cita ON entregas_notificacion(empresa_id, entidad_id, creada_en DESC);
CREATE INDEX idx_outbox_notificaciones_pendientes ON outbox_notificaciones(estado, proximo_intento);
