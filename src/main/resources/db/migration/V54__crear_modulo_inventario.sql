CREATE TABLE categorias_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    nombre VARCHAR(100) NOT NULL, activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, nombre)
);
CREATE TABLE unidades_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    nombre VARCHAR(60) NOT NULL, abreviatura VARCHAR(20), activa BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (empresa_id, nombre)
);
CREATE TABLE productos_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    codigo VARCHAR(40) NOT NULL, codigo_barras VARCHAR(80), nombre VARCHAR(180) NOT NULL,
    categoria_id UUID NOT NULL REFERENCES categorias_inventario(id), unidad_id UUID NOT NULL REFERENCES unidades_inventario(id),
    descripcion TEXT, stock_minimo NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    controla_vencimiento BOOLEAN NOT NULL DEFAULT FALSE, activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por UUID REFERENCES usuarios(id), creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, codigo)
);
CREATE TABLE existencias_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    sucursal_id UUID NOT NULL REFERENCES sucursales(id), producto_id UUID NOT NULL REFERENCES productos_inventario(id),
    cantidad NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (cantidad >= 0), version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (empresa_id, sucursal_id, producto_id)
);
CREATE TABLE lotes_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    sucursal_id UUID NOT NULL REFERENCES sucursales(id), producto_id UUID NOT NULL REFERENCES productos_inventario(id),
    numero_lote VARCHAR(80) NOT NULL, fecha_vencimiento DATE, cantidad NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, sucursal_id, producto_id, numero_lote)
);
CREATE TABLE movimientos_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id),
    sucursal_id UUID NOT NULL REFERENCES sucursales(id), producto_id UUID NOT NULL REFERENCES productos_inventario(id),
    lote_id UUID REFERENCES lotes_inventario(id), numero VARCHAR(40) NOT NULL, tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','AJUSTE')),
    motivo VARCHAR(80) NOT NULL, cantidad NUMERIC(14,2) NOT NULL CHECK (cantidad <> 0),
    stock_anterior NUMERIC(14,2) NOT NULL, stock_posterior NUMERIC(14,2) NOT NULL,
    costo_unitario NUMERIC(14,2), referencia VARCHAR(150), observacion TEXT,
    creado_por UUID REFERENCES usuarios(id), creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (empresa_id, numero)
);
CREATE INDEX idx_productos_inventario_empresa_activo ON productos_inventario(empresa_id, activo);
CREATE INDEX idx_existencias_inventario_empresa_sucursal ON existencias_inventario(empresa_id, sucursal_id);
CREATE INDEX idx_lotes_inventario_vencimiento ON lotes_inventario(empresa_id, fecha_vencimiento);
CREATE INDEX idx_movimientos_inventario_empresa_fecha ON movimientos_inventario(empresa_id, creado_en DESC);
INSERT INTO categorias_inventario (empresa_id, nombre)
SELECT e.id, c.nombre FROM empresas e CROSS JOIN (VALUES ('Medicamentos'), ('Material médico'), ('Insumos'), ('Consumibles'), ('Laboratorio'), ('Limpieza'), ('Otros')) AS c(nombre)
ON CONFLICT (empresa_id, nombre) DO NOTHING;
INSERT INTO unidades_inventario (empresa_id, nombre, abreviatura)
SELECT e.id, u.nombre, u.abreviatura FROM empresas e CROSS JOIN (VALUES ('Unidad','und'),('Caja','caja'),('Paquete','paq'),('Frasco','frasco'),('Ampolla','amp'),('Tableta','tab'),('Mililitro','ml'),('Litro','l'),('Rollo','rollo'),('Par','par')) AS u(nombre, abreviatura)
ON CONFLICT (empresa_id, nombre) DO NOTHING;
INSERT INTO permisos (codigo,nombre,descripcion) VALUES
 ('INVENTORY_VIEW','Ver inventario','Consultar productos y existencias'),('INVENTORY_CREATE','Crear productos','Registrar productos'),('INVENTORY_EDIT','Editar productos','Modificar productos'),('INVENTORY_MOVEMENT','Registrar movimientos','Registrar entradas, salidas y ajustes')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id,permiso_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre='ADMINISTRADOR' AND p.codigo IN ('INVENTORY_VIEW','INVENTORY_CREATE','INVENTORY_EDIT','INVENTORY_MOVEMENT') ON CONFLICT DO NOTHING;
