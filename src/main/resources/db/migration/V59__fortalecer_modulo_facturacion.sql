-- Facturacion conserva las tablas existentes y agrega el contrato comercial,
-- los snapshots historicos y los controles transaccionales que faltaban.

ALTER TABLE facturas ADD COLUMN IF NOT EXISTS sucursal_id UUID REFERENCES sucursales(id);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS medico_id UUID REFERENCES medicos(id);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS tipo_comprobante VARCHAR(30);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS moneda VARCHAR(3) NOT NULL DEFAULT 'DOP';
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS origen_tipo VARCHAR(40);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS origen_id UUID;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS observacion TEXT;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS clave_idempotencia VARCHAR(100);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS estado_ecf VARCHAR(30) NOT NULL DEFAULT 'NO_APLICA';
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emitido_por UUID REFERENCES usuarios(id);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emitido_en TIMESTAMP;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS anulado_por UUID REFERENCES usuarios(id);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS anulado_en TIMESTAMP;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS motivo_anulacion VARCHAR(120);
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS observacion_anulacion TEXT;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE facturas SET estado = 'PENDIENTE' WHERE estado = 'EMITIDA';

ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS producto_id UUID REFERENCES productos_inventario(id);
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS tipo_item VARCHAR(20) NOT NULL DEFAULT 'SERVICIO';
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS codigo_snapshot VARCHAR(80);
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS tasa_impuesto_snapshot NUMERIC(7,4) NOT NULL DEFAULT 0;
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS impuesto NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS subtotal NUMERIC(14,2);
ALTER TABLE detalle_factura ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE detalle_factura d
SET codigo_snapshot = COALESCE(d.codigo_snapshot, s.codigo),
    subtotal = COALESCE(d.subtotal, ROUND(d.cantidad * d.precio, 2)),
    tipo_item = 'SERVICIO'
FROM servicios s
WHERE d.servicio_id = s.id;

UPDATE detalle_factura
SET codigo_snapshot = COALESCE(codigo_snapshot, 'LEGACY'),
    subtotal = COALESCE(subtotal, ROUND(cantidad * precio, 2));

ALTER TABLE detalle_factura ALTER COLUMN subtotal SET NOT NULL;
ALTER TABLE detalle_factura ALTER COLUMN codigo_snapshot SET NOT NULL;

-- Configuracion fiscal por elemento. El valor inicial cero evita asumir ITBIS.
ALTER TABLE servicios ADD COLUMN IF NOT EXISTS tasa_impuesto NUMERIC(7,4) NOT NULL DEFAULT 0;
ALTER TABLE productos_inventario ADD COLUMN IF NOT EXISTS tasa_impuesto NUMERIC(7,4) NOT NULL DEFAULT 0;

CREATE TABLE consecutivos_facturacion (
  empresa_id UUID PRIMARY KEY REFERENCES empresas(id) ON DELETE CASCADE,
  siguiente_factura BIGINT NOT NULL DEFAULT 1,
  CHECK (siguiente_factura > 0)
);

INSERT INTO consecutivos_facturacion (empresa_id, siguiente_factura)
SELECT e.id,
       COALESCE(MAX(CASE WHEN f.numero ~ '^FAC-[0-9]+$'
                         THEN substring(f.numero FROM 5)::BIGINT END), 0) + 1
FROM empresas e
LEFT JOIN facturas f ON f.empresa_id = e.id
GROUP BY e.id
ON CONFLICT (empresa_id) DO NOTHING;

CREATE TABLE historial_factura (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  factura_id UUID NOT NULL REFERENCES facturas(id),
  estado_anterior VARCHAR(20),
  estado_nuevo VARCHAR(20) NOT NULL,
  accion VARCHAR(40) NOT NULL,
  motivo TEXT,
  usuario_id UUID REFERENCES usuarios(id),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_facturas_empresa_id ON facturas(empresa_id, id);
CREATE UNIQUE INDEX uq_sucursales_empresa_id ON sucursales(empresa_id, id);
CREATE UNIQUE INDEX uq_pacientes_empresa_id ON pacientes(empresa_id, id);
CREATE UNIQUE INDEX uq_medicos_empresa_id ON medicos(empresa_id, id);
CREATE UNIQUE INDEX uq_servicios_empresa_id ON servicios(empresa_id, id);
CREATE UNIQUE INDEX uq_productos_inventario_empresa_id ON productos_inventario(empresa_id, id);

ALTER TABLE facturas ADD CONSTRAINT fk_factura_sucursal_tenant
  FOREIGN KEY (empresa_id, sucursal_id) REFERENCES sucursales(empresa_id, id);
ALTER TABLE facturas ADD CONSTRAINT fk_factura_paciente_tenant
  FOREIGN KEY (empresa_id, paciente_id) REFERENCES pacientes(empresa_id, id);
ALTER TABLE facturas ADD CONSTRAINT fk_factura_medico_tenant
  FOREIGN KEY (empresa_id, medico_id) REFERENCES medicos(empresa_id, id);
ALTER TABLE detalle_factura ADD CONSTRAINT fk_detalle_factura_tenant
  FOREIGN KEY (empresa_id, factura_id) REFERENCES facturas(empresa_id, id);
ALTER TABLE detalle_factura ADD CONSTRAINT fk_detalle_servicio_tenant
  FOREIGN KEY (empresa_id, servicio_id) REFERENCES servicios(empresa_id, id);
ALTER TABLE detalle_factura ADD CONSTRAINT fk_detalle_producto_tenant
  FOREIGN KEY (empresa_id, producto_id) REFERENCES productos_inventario(empresa_id, id);
ALTER TABLE historial_factura ADD CONSTRAINT fk_historial_factura_tenant
  FOREIGN KEY (empresa_id, factura_id) REFERENCES facturas(empresa_id, id);
ALTER TABLE cargos_financieros ADD CONSTRAINT fk_cargo_factura_tenant
  FOREIGN KEY (empresa_id, factura_id) REFERENCES facturas(empresa_id, id);

ALTER TABLE facturas ADD CONSTRAINT ck_facturas_estado
  CHECK (estado IN ('BORRADOR','PENDIENTE','PARCIAL','PAGADA','ANULADA'));
ALTER TABLE facturas ADD CONSTRAINT ck_facturas_montos
  CHECK (subtotal >= 0 AND descuento >= 0 AND impuestos >= 0 AND total >= 0
         AND monto_pagado >= 0 AND saldo >= 0 AND monto_pagado <= total AND saldo <= total);
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_tipo
  CHECK ((tipo_item = 'SERVICIO' AND servicio_id IS NOT NULL AND producto_id IS NULL)
      OR (tipo_item = 'PRODUCTO' AND producto_id IS NOT NULL AND servicio_id IS NULL)) NOT VALID;
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_montos
  CHECK (cantidad > 0 AND precio >= 0 AND descuento >= 0 AND impuesto >= 0
         AND subtotal >= 0 AND importe >= 0 AND tasa_impuesto_snapshot >= 0);
ALTER TABLE servicios ADD CONSTRAINT ck_servicios_tasa_impuesto
  CHECK (tasa_impuesto >= 0 AND tasa_impuesto <= 100);
ALTER TABLE productos_inventario ADD CONSTRAINT ck_productos_tasa_impuesto
  CHECK (tasa_impuesto >= 0 AND tasa_impuesto <= 100);

CREATE UNIQUE INDEX uq_facturas_empresa_idempotencia
  ON facturas(empresa_id, clave_idempotencia) WHERE clave_idempotencia IS NOT NULL;
CREATE UNIQUE INDEX uq_facturas_empresa_origen
  ON facturas(empresa_id, origen_tipo, origen_id) WHERE origen_tipo IS NOT NULL AND origen_id IS NOT NULL;
CREATE INDEX idx_facturas_empresa_fecha ON facturas(empresa_id, fecha DESC);
CREATE INDEX idx_facturas_empresa_sucursal_fecha ON facturas(empresa_id, sucursal_id, fecha DESC);
CREATE INDEX idx_facturas_empresa_paciente ON facturas(empresa_id, paciente_id, fecha DESC);
CREATE INDEX idx_facturas_empresa_medico ON facturas(empresa_id, medico_id, fecha DESC);
CREATE INDEX idx_detalle_factura_empresa_factura ON detalle_factura(empresa_id, factura_id);
CREATE INDEX idx_historial_factura_empresa_factura ON historial_factura(empresa_id, factura_id, creado_en DESC);

INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('BILLING_VIEW','Consultar facturas','Consultar facturas y su detalle'),
 ('BILLING_CREATE','Crear facturas','Crear borradores de factura'),
 ('BILLING_EDIT_DRAFT','Editar borradores','Modificar facturas en borrador'),
 ('BILLING_ISSUE','Emitir facturas','Emitir facturas y generar cuentas por cobrar'),
 ('BILLING_REGISTER_PAYMENT','Registrar pagos de factura','Abrir Pagos desde una factura'),
 ('BILLING_APPLY_DISCOUNT','Aplicar descuentos','Aplicar descuentos autorizados'),
 ('BILLING_CHANGE_PRICE','Modificar precio de factura','Modificar el precio sugerido de un elemento'),
 ('BILLING_VOID','Anular facturas','Anular facturas emitidas sin pagos aplicados'),
 ('BILLING_PRINT','Imprimir facturas','Generar e imprimir el PDF de una factura'),
 ('BILLING_EXPORT','Exportar facturas','Exportar listados de facturas')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMINISTRADOR','SUPERADMIN') AND p.codigo LIKE 'BILLING_%'
ON CONFLICT DO NOTHING;
