DROP INDEX IF EXISTS uq_sesion_caja_abierta_usuario_sucursal;
CREATE UNIQUE INDEX uq_sesion_caja_abierta_por_caja ON sesiones_caja(empresa_id,caja_id) WHERE estado='OPEN' AND caja_id IS NOT NULL;
ALTER TABLE consecutivos_financieros ADD COLUMN IF NOT EXISTS siguiente_cierre_caja BIGINT NOT NULL DEFAULT 1;
ALTER TABLE sesiones_caja ADD COLUMN IF NOT EXISTS numero_cierre VARCHAR(40);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_caja_numero_cierre ON sesiones_caja(empresa_id,numero_cierre) WHERE numero_cierre IS NOT NULL;
CREATE OR REPLACE FUNCTION asignar_numero_cierre_caja() RETURNS trigger AS $$
DECLARE n BIGINT;
BEGIN
 IF NEW.estado='CLOSED' AND (OLD.estado IS DISTINCT FROM 'CLOSED') AND NEW.numero_cierre IS NULL THEN
   INSERT INTO consecutivos_financieros(empresa_id) VALUES (NEW.empresa_id) ON CONFLICT (empresa_id) DO NOTHING;
   UPDATE consecutivos_financieros SET siguiente_cierre_caja=siguiente_cierre_caja+1 WHERE empresa_id=NEW.empresa_id RETURNING siguiente_cierre_caja-1 INTO n;
   NEW.numero_cierre='CIE-'||LPAD(n::text,6,'0');
 END IF;
 RETURN NEW;
END $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_numero_cierre_caja ON sesiones_caja;
CREATE TRIGGER trg_numero_cierre_caja BEFORE UPDATE ON sesiones_caja FOR EACH ROW EXECUTE FUNCTION asignar_numero_cierre_caja();
