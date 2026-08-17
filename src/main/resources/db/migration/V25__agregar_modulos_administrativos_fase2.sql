-- Fase 2: las entidades de negocio conservan siempre el aislamiento por empresa.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS preferencia_tema VARCHAR(12) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE citas ADD COLUMN IF NOT EXISTS origen VARCHAR(20) NOT NULL DEFAULT 'INTERNA';

CREATE TABLE servicios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  codigo VARCHAR(40) NOT NULL, nombre VARCHAR(120) NOT NULL, descripcion TEXT, precio NUMERIC(14,2) NOT NULL DEFAULT 0,
  activo BOOLEAN NOT NULL DEFAULT TRUE, creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (empresa_id, codigo)
);
CREATE TABLE facturas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID NOT NULL REFERENCES pacientes(id), numero VARCHAR(40) NOT NULL, fecha DATE NOT NULL,
  subtotal NUMERIC(14,2) NOT NULL DEFAULT 0, descuento NUMERIC(14,2) NOT NULL DEFAULT 0,
  impuestos NUMERIC(14,2) NOT NULL DEFAULT 0, total NUMERIC(14,2) NOT NULL DEFAULT 0,
  monto_pagado NUMERIC(14,2) NOT NULL DEFAULT 0, saldo NUMERIC(14,2) NOT NULL DEFAULT 0,
  estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR', creado_por UUID REFERENCES usuarios(id), creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (empresa_id, numero)
);
CREATE TABLE detalle_factura (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  factura_id UUID NOT NULL REFERENCES facturas(id) ON DELETE CASCADE, servicio_id UUID REFERENCES servicios(id),
  descripcion VARCHAR(255) NOT NULL, cantidad NUMERIC(12,2) NOT NULL, precio NUMERIC(14,2) NOT NULL,
  descuento NUMERIC(14,2) NOT NULL DEFAULT 0, importe NUMERIC(14,2) NOT NULL
);
CREATE TABLE pagos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  factura_id UUID NOT NULL REFERENCES facturas(id), fecha DATE NOT NULL, monto NUMERIC(14,2) NOT NULL,
  metodo_pago VARCHAR(20) NOT NULL, referencia VARCHAR(120), observacion TEXT, usuario_id UUID REFERENCES usuarios(id),
  estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO', creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE recordatorios_cita (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cita_id UUID NOT NULL REFERENCES citas(id) ON DELETE CASCADE, canal VARCHAR(20) NOT NULL, fecha_programada TIMESTAMP NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', fecha_envio TIMESTAMP, resultado TEXT, creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE historial_clinico (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE, alergias TEXT, antecedentes TEXT, resumen TEXT,
  actualizado_por UUID REFERENCES usuarios(id), actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (empresa_id, paciente_id)
);
CREATE TABLE auditoria (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  usuario_id UUID REFERENCES usuarios(id), fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, modulo VARCHAR(80) NOT NULL,
  accion VARCHAR(40) NOT NULL, entidad VARCHAR(80), registro_id UUID, ip VARCHAR(64), datos_anteriores TEXT, datos_nuevos TEXT
);
CREATE TABLE configuracion_empresa_fase2 (
  empresa_id UUID PRIMARY KEY REFERENCES empresas(id) ON DELETE CASCADE, requiere_aprobacion_citas BOOLEAN NOT NULL DEFAULT FALSE,
  prefijo_factura VARCHAR(12) NOT NULL DEFAULT 'FAC', siguiente_factura INTEGER NOT NULL DEFAULT 1,
  recordatorios_activos BOOLEAN NOT NULL DEFAULT TRUE, actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_facturas_empresa_estado ON facturas(empresa_id, estado);
CREATE INDEX idx_pagos_empresa_fecha ON pagos(empresa_id, fecha);
CREATE INDEX idx_recordatorios_empresa_estado ON recordatorios_cita(empresa_id, estado);
CREATE INDEX idx_auditoria_empresa_fecha ON auditoria(empresa_id, fecha DESC);

INSERT INTO permisos (codigo, nombre, descripcion) VALUES
 ('MENU_APROBACION_CITAS','Aprobación de citas','Gestionar solicitudes de cita'), ('MENU_RECORDATORIOS','Recordatorios','Gestionar recordatorios'),
 ('MENU_HISTORIAL_CLINICO','Historial clínico','Consultar información clínica'), ('MENU_DOCUMENTOS','Documentos','Gestionar documentos'),
 ('MENU_FACTURACION','Facturación','Gestionar facturas y servicios'), ('MENU_PAGOS','Pagos','Registrar pagos'),
 ('MENU_CUENTAS_COBRAR','Cuentas por cobrar','Consultar saldos pendientes'), ('MENU_REPORTES','Reportes','Consultar reportes'),
 ('MENU_CONFIGURACION_SISTEMA','Configuración del sistema','Administrar preferencias institucionales'), ('MENU_AUDITORIA','Auditoría','Consultar operaciones')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre = 'ADMINISTRADOR' ON CONFLICT DO NOTHING;
