CREATE TABLE sesiones_caja (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id),
  numero VARCHAR(40) NOT NULL,
  estado VARCHAR(12) NOT NULL DEFAULT 'OPEN',
  abierto_por UUID NOT NULL REFERENCES usuarios(id),
  abierto_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fondo_inicial NUMERIC(14,2) NOT NULL CHECK (fondo_inicial >= 0),
  nota_apertura TEXT,
  efectivo_esperado NUMERIC(14,2), efectivo_contado NUMERIC(14,2), diferencia NUMERIC(14,2),
  cerrado_por UUID REFERENCES usuarios(id), cerrado_en TIMESTAMP, nota_cierre TEXT, motivo_diferencia TEXT,
  version BIGINT NOT NULL DEFAULT 0,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_sesion_caja_estado CHECK (estado IN ('OPEN','CLOSED')),
  CONSTRAINT uq_sesion_caja_numero UNIQUE (empresa_id, numero)
);
CREATE UNIQUE INDEX uq_sesion_caja_abierta_usuario_sucursal ON sesiones_caja(empresa_id,sucursal_id,abierto_por) WHERE estado='OPEN';
CREATE INDEX idx_sesiones_caja_historial ON sesiones_caja(empresa_id,sucursal_id,abierto_en DESC);

CREATE TABLE movimientos_caja (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id),
  sesion_caja_id UUID NOT NULL REFERENCES sesiones_caja(id),
  numero VARCHAR(40) NOT NULL,
  tipo VARCHAR(30) NOT NULL, direccion VARCHAR(3) NOT NULL,
  metodo_pago VARCHAR(20), monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
  pago_id UUID REFERENCES pagos(id), reembolso_id UUID REFERENCES reembolsos_pago(id),
  concepto VARCHAR(255) NOT NULL, referencia VARCHAR(120), notas TEXT,
  estado VARCHAR(12) NOT NULL DEFAULT 'ACTIVE', creado_por UUID NOT NULL REFERENCES usuarios(id), creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  revertido_por UUID REFERENCES usuarios(id), revertido_en TIMESTAMP, motivo_reversion TEXT,
  CONSTRAINT ck_movimiento_caja_direccion CHECK (direccion IN ('IN','OUT')),
  CONSTRAINT ck_movimiento_caja_estado CHECK (estado IN ('ACTIVE','REVERSED')),
  CONSTRAINT uq_movimiento_caja_numero UNIQUE (empresa_id, numero),
  CONSTRAINT uq_movimiento_caja_pago UNIQUE (pago_id), CONSTRAINT uq_movimiento_caja_reembolso UNIQUE (reembolso_id)
);
CREATE INDEX idx_movimientos_caja_sesion ON movimientos_caja(empresa_id,sesion_caja_id,creado_en);

ALTER TABLE consecutivos_financieros ADD COLUMN IF NOT EXISTS siguiente_caja BIGINT NOT NULL DEFAULT 1;
ALTER TABLE consecutivos_financieros ADD COLUMN IF NOT EXISTS siguiente_movimiento_caja BIGINT NOT NULL DEFAULT 1;

INSERT INTO permisos (codigo,nombre,descripcion) VALUES
 ('CASH_VIEW','Consultar caja','Ver la caja propia'),('CASH_OPEN','Abrir caja','Abrir una sesión de caja'),
 ('CASH_CREATE_INCOME','Registrar ingreso de caja','Registrar ingresos manuales'),('CASH_CREATE_EXPENSE','Registrar egreso de caja','Registrar egresos manuales'),
 ('CASH_CLOSE','Cerrar caja','Realizar arqueo y cierre'),('CASH_CLOSE_WITH_DIFFERENCE','Cerrar caja con diferencia','Autorizar cierre con faltante o sobrante'),
 ('CASH_REVERSE_MOVEMENT','Reversar movimiento de caja','Reversar movimientos manuales'),('CASH_VIEW_HISTORY','Consultar histórico de caja','Consultar sesiones cerradas'),
 ('CASH_PRINT_CLOSE','Imprimir cierre de caja','Generar el reporte de cierre'),('CASH_VIEW_ALL','Consultar todas las cajas','Ver cajas de otros responsables')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id,permiso_id) SELECT r.id,p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre='ADMINISTRADOR' AND p.codigo LIKE 'CASH_%' ON CONFLICT DO NOTHING;
