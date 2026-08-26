-- Las notificaciones históricas conservan su destinatario original. Las nuevas
-- notificaciones se comparten mediante destinatarios individuales.
CREATE TABLE notification_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notificaciones(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES usuarios(id),
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    leida_en TIMESTAMP,
    creada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_recipient UNIQUE (notification_id, user_id)
);

INSERT INTO notification_recipients (notification_id, user_id, leida, leida_en, creada_en)
SELECT id, usuario_id, leida, leida_en, creada_en
FROM notificaciones
WHERE usuario_id IS NOT NULL
ON CONFLICT (notification_id, user_id) DO NOTHING;

-- usuario_id, leida y leida_en se conservan únicamente para compatibilidad
-- histórica; el estado visible se obtiene siempre desde notification_recipients.
ALTER TABLE notificaciones ALTER COLUMN usuario_id DROP NOT NULL;

CREATE INDEX idx_notification_recipients_usuario_no_leidas
    ON notification_recipients(user_id, leida, creada_en DESC);
