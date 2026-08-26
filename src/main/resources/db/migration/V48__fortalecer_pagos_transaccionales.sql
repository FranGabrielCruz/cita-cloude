-- Pagos deja de depender obligatoriamente de una factura y se convierte en el
-- registro transaccional financiero. Las facturas existentes se conservan.
ALTER TABLE pagos ALTER COLUMN factura_id DROP NOT NULL;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS numero VARCHAR(40);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS paciente_id UUID REFERENCES pacientes(id);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS sucursal_id UUID REFERENCES sucursales(id);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS moneda VARCHAR(3) NOT NULL DEFAULT 'DOP';
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS efectivo_recibido NUMERIC(14,2);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS cambio NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS autorizacion VARCHAR(120);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS clave_idempotencia VARCHAR(100);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS anulado_en TIMESTAMP;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS anulado_por UUID REFERENCES usuarios(id);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS motivo_anulacion TEXT;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE pagos p SET paciente_id = f.paciente_id
FROM facturas f WHERE p.factura_id = f.id AND p.paciente_id IS NULL;

CREATE TABLE cargos_financieros (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID NOT NULL REFERENCES pacientes(id),
  sucursal_id UUID REFERENCES sucursales(id),
  factura_id UUID REFERENCES facturas(id),
  origen VARCHAR(30) NOT NULL,
  referencia_origen VARCHAR(80),
  concepto VARCHAR(255) NOT NULL,
  fecha DATE NOT NULL,
  monto_original NUMERIC(14,2) NOT NULL,
  monto_pagado NUMERIC(14,2) NOT NULL DEFAULT 0,
  saldo NUMERIC(14,2) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  version BIGINT NOT NULL DEFAULT 0,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (monto_original >= 0), CHECK (monto_pagado >= 0), CHECK (saldo >= 0),
  UNIQUE (empresa_id, factura_id)
);

CREATE TABLE pago_aplicaciones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  pago_id UUID NOT NULL REFERENCES pagos(id),
  cargo_id UUID NOT NULL REFERENCES cargos_financieros(id),
  monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (pago_id, cargo_id)
);

CREATE TABLE reembolsos_pago (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  pago_id UUID NOT NULL REFERENCES pagos(id),
  monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
  metodo_pago VARCHAR(20) NOT NULL,
  referencia VARCHAR(120), motivo TEXT NOT NULL,
  reembolsado_por UUID REFERENCES usuarios(id), reembolsado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consecutivos_financieros (
  empresa_id UUID PRIMARY KEY REFERENCES empresas(id) ON DELETE CASCADE,
  siguiente_pago BIGINT NOT NULL DEFAULT 1
);

INSERT INTO cargos_financieros (empresa_id, paciente_id, factura_id, origen, referencia_origen, concepto, fecha, monto_original, monto_pagado, saldo, estado)
SELECT empresa_id, paciente_id, id, 'FACTURA', numero, 'Factura ' || numero, fecha, total, monto_pagado, saldo,
       CASE WHEN saldo = 0 THEN 'PAGADA' WHEN monto_pagado > 0 THEN 'PARCIAL' ELSE 'PENDIENTE' END
FROM facturas WHERE estado <> 'ANULADA'
ON CONFLICT (empresa_id, factura_id) DO NOTHING;

CREATE UNIQUE INDEX uq_pagos_empresa_numero ON pagos(empresa_id, numero) WHERE numero IS NOT NULL;
CREATE UNIQUE INDEX uq_pagos_empresa_idempotencia ON pagos(empresa_id, clave_idempotencia) WHERE clave_idempotencia IS NOT NULL;
CREATE INDEX idx_cargos_financieros_pendientes ON cargos_financieros(empresa_id, paciente_id, saldo, fecha);
CREATE INDEX idx_pagos_empresa_creado ON pagos(empresa_id, creado_en DESC);
CREATE INDEX idx_pago_aplicaciones_pago ON pago_aplicaciones(empresa_id, pago_id);

INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('PAYMENTS_CREATE','Registrar pagos','Registrar pagos y aplicar cargos'),
 ('PAYMENTS_VIEW','Consultar pagos','Consultar pagos y recibos'),
 ('PAYMENTS_VOID','Anular pagos','Anular pagos aplicados'),
 ('PAYMENTS_REFUND','Reembolsar pagos','Registrar reembolsos de pagos')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR' AND p.codigo IN ('PAYMENTS_CREATE','PAYMENTS_VIEW','PAYMENTS_VOID','PAYMENTS_REFUND')
ON CONFLICT DO NOTHING;
