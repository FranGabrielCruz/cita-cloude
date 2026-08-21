ALTER TABLE pacientes ALTER COLUMN documento DROP NOT NULL;
ALTER TABLE pacientes ADD COLUMN numero_expediente VARCHAR(30);
ALTER TABLE pacientes ADD COLUMN nacionalidad VARCHAR(80), ADD COLUMN provincia VARCHAR(100), ADD COLUMN municipio VARCHAR(100), ADD COLUMN telefono_alternativo VARCHAR(30), ADD COLUMN contacto_emergencia VARCHAR(200), ADD COLUMN telefono_emergencia VARCHAR(30), ADD COLUMN parentesco_emergencia VARCHAR(80);
WITH expedientes AS (
    SELECT id, 'HC-' || LPAD(ROW_NUMBER() OVER (PARTITION BY empresa_id ORDER BY creado_en)::TEXT, 7, '0') AS numero
    FROM pacientes
    WHERE numero_expediente IS NULL
)
UPDATE pacientes paciente
SET numero_expediente = expedientes.numero
FROM expedientes
WHERE paciente.id = expedientes.id;
ALTER TABLE pacientes ALTER COLUMN numero_expediente SET NOT NULL;
CREATE UNIQUE INDEX uq_pacientes_empresa_expediente ON pacientes(empresa_id, numero_expediente);
