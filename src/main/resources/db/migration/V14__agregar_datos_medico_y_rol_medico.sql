ALTER TABLE medicos
    ADD COLUMN cedula VARCHAR(50),
    ADD COLUMN sucursal_id UUID REFERENCES sucursales(id) ON DELETE SET NULL;

CREATE INDEX idx_medicos_empresa_sucursal ON medicos(empresa_id, sucursal_id);

INSERT INTO roles (empresa_id, nombre, descripcion)
SELECT e.id, 'MEDICO', 'Acceso para mÃ©dicos'
FROM empresas e
WHERE NOT EXISTS (
    SELECT 1
    FROM roles r
    WHERE r.empresa_id = e.id
      AND r.nombre = 'MEDICO'
);
