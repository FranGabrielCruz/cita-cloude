ALTER TABLE productos_inventario
    ADD COLUMN es_facturable BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN precio_venta NUMERIC(18,2);

ALTER TABLE productos_inventario
    ADD CONSTRAINT chk_productos_inventario_precio_venta
    CHECK ((es_facturable = FALSE AND precio_venta IS NULL) OR (es_facturable = TRUE AND precio_venta > 0));

CREATE INDEX idx_productos_inventario_facturables
    ON productos_inventario(empresa_id, activo, es_facturable)
    WHERE es_facturable = TRUE AND activo = TRUE;
