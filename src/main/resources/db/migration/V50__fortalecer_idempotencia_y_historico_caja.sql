ALTER TABLE sesiones_caja ADD COLUMN IF NOT EXISTS clave_cierre VARCHAR(100);
ALTER TABLE movimientos_caja ADD COLUMN IF NOT EXISTS clave_idempotencia VARCHAR(100);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_caja_clave_cierre ON sesiones_caja(empresa_id,clave_cierre) WHERE clave_cierre IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_movimiento_caja_idempotencia ON movimientos_caja(empresa_id,clave_idempotencia) WHERE clave_idempotencia IS NOT NULL;
