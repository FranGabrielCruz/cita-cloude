CREATE TABLE documentos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID REFERENCES pacientes(id) ON DELETE SET NULL,
  nombre VARCHAR(255) NOT NULL, tipo VARCHAR(100) NOT NULL, descripcion TEXT,
  ruta_archivo VARCHAR(500) NOT NULL, nombre_archivo VARCHAR(255) NOT NULL,
  usuario_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_documentos_empresa_paciente ON documentos(empresa_id, paciente_id);
