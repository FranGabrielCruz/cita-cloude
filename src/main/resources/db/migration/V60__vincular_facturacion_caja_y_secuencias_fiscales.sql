CREATE TABLE secuencias_comprobante_fiscal (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  tipo VARCHAR(30) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  prefijo VARCHAR(20) NOT NULL,
  numero_desde BIGINT NOT NULL,
  numero_hasta BIGINT NOT NULL,
  numero_siguiente BIGINT NOT NULL,
  activa BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uq_secuencia_fiscal_empresa_tipo UNIQUE (empresa_id, tipo),
  CONSTRAINT ck_secuencia_fiscal_rango CHECK (
    numero_desde >= 0 AND numero_hasta >= numero_desde
    AND numero_siguiente >= numero_desde AND numero_siguiente <= numero_hasta + 1
  )
);

CREATE UNIQUE INDEX uq_cajas_empresa_id ON cajas(empresa_id, id);
CREATE UNIQUE INDEX uq_sesiones_caja_empresa_id ON sesiones_caja(empresa_id, id);
CREATE UNIQUE INDEX uq_secuencias_fiscales_empresa_id ON secuencias_comprobante_fiscal(empresa_id, id);

ALTER TABLE facturas ADD COLUMN caja_id UUID REFERENCES cajas(id);
ALTER TABLE facturas ADD COLUMN sesion_caja_id UUID REFERENCES sesiones_caja(id);
ALTER TABLE facturas ADD COLUMN secuencia_comprobante_id UUID REFERENCES secuencias_comprobante_fiscal(id);
ALTER TABLE facturas ADD COLUMN numero_comprobante_fiscal VARCHAR(40);

ALTER TABLE facturas ADD CONSTRAINT fk_factura_caja_tenant
  FOREIGN KEY (empresa_id, caja_id) REFERENCES cajas(empresa_id, id);
ALTER TABLE facturas ADD CONSTRAINT fk_factura_sesion_caja_tenant
  FOREIGN KEY (empresa_id, sesion_caja_id) REFERENCES sesiones_caja(empresa_id, id);
ALTER TABLE facturas ADD CONSTRAINT fk_factura_secuencia_fiscal_tenant
  FOREIGN KEY (empresa_id, secuencia_comprobante_id) REFERENCES secuencias_comprobante_fiscal(empresa_id, id);

ALTER TABLE facturas ADD CONSTRAINT ck_factura_emitida_con_caja
  CHECK (estado = 'BORRADOR' OR (caja_id IS NOT NULL AND sesion_caja_id IS NOT NULL)) NOT VALID;
ALTER TABLE facturas ADD CONSTRAINT ck_factura_comprobante_asignado
  CHECK (tipo_comprobante IS NULL OR estado = 'BORRADOR' OR numero_comprobante_fiscal IS NOT NULL) NOT VALID;

CREATE UNIQUE INDEX uq_factura_empresa_comprobante_fiscal
  ON facturas(empresa_id, numero_comprobante_fiscal) WHERE numero_comprobante_fiscal IS NOT NULL;
CREATE INDEX idx_facturas_empresa_caja ON facturas(empresa_id, caja_id, fecha DESC);
CREATE INDEX idx_secuencias_fiscales_empresa_activa ON secuencias_comprobante_fiscal(empresa_id, activa, tipo);

INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('BILLING_FISCAL_CONFIG','Configurar secuencias fiscales','Administrar rangos de comprobantes fiscales')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMINISTRADOR','SUPERADMIN') AND p.codigo = 'BILLING_FISCAL_CONFIG'
ON CONFLICT DO NOTHING;
