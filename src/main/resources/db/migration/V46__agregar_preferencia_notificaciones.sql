ALTER TABLE configuracion_empresa_fase2
  ADD COLUMN IF NOT EXISTS notificaciones_activas BOOLEAN NOT NULL DEFAULT TRUE;
