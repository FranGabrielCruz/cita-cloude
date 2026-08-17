CREATE TABLE check_ins (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cita_id UUID NOT NULL REFERENCES citas(id) ON DELETE CASCADE, paciente_id UUID NOT NULL REFERENCES pacientes(id),
  usuario_id UUID REFERENCES usuarios(id), fecha_hora_llegada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (empresa_id, cita_id)
);
CREATE TABLE signos_vitales (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cita_id UUID REFERENCES citas(id), paciente_id UUID NOT NULL REFERENCES pacientes(id), peso NUMERIC(6,2), altura NUMERIC(5,2),
  temperatura NUMERIC(4,1), presion_sistolica INTEGER, presion_diastolica INTEGER, frecuencia_cardiaca INTEGER,
  frecuencia_respiratoria INTEGER, saturacion_oxigeno NUMERIC(5,2), imc NUMERIC(6,2), observaciones TEXT,
  usuario_id UUID REFERENCES usuarios(id), fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE consultas_medicas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cita_id UUID NOT NULL REFERENCES citas(id), paciente_id UUID NOT NULL REFERENCES pacientes(id), medico_id UUID NOT NULL REFERENCES medicos(id),
  motivo TEXT, historia_actual TEXT, examen_observaciones TEXT, estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, finalizado_en TIMESTAMP
);
CREATE TABLE antecedentes_medicos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID NOT NULL REFERENCES pacientes(id), tipo VARCHAR(40) NOT NULL, descripcion TEXT NOT NULL,
  fecha_aproximada DATE, estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', observaciones TEXT
);
CREATE TABLE alergias_paciente (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  paciente_id UUID NOT NULL REFERENCES pacientes(id), alergeno VARCHAR(150) NOT NULL, tipo VARCHAR(50), reaccion TEXT,
  severidad VARCHAR(15) NOT NULL DEFAULT 'LEVE', observaciones TEXT, estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
);
CREATE TABLE diagnosticos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  consulta_id UUID REFERENCES consultas_medicas(id) ON DELETE CASCADE, paciente_id UUID NOT NULL REFERENCES pacientes(id), medico_id UUID REFERENCES medicos(id),
  codigo VARCHAR(30), descripcion TEXT NOT NULL, tipo VARCHAR(30), principal BOOLEAN NOT NULL DEFAULT FALSE, observaciones TEXT, fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE tratamientos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  consulta_id UUID REFERENCES consultas_medicas(id) ON DELETE CASCADE, paciente_id UUID NOT NULL REFERENCES pacientes(id), diagnostico_id UUID REFERENCES diagnosticos(id),
  descripcion TEXT NOT NULL, fecha_inicio DATE, fecha_fin DATE, indicaciones TEXT, estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);
CREATE TABLE recetas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  consulta_id UUID REFERENCES consultas_medicas(id) ON DELETE CASCADE, paciente_id UUID NOT NULL REFERENCES pacientes(id), medico_id UUID REFERENCES medicos(id), creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE detalle_receta (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), receta_id UUID NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
  medicamento VARCHAR(180) NOT NULL, dosis VARCHAR(100), frecuencia VARCHAR(100), duracion VARCHAR(100), via_administracion VARCHAR(100), indicaciones TEXT
);
CREATE TABLE ordenes_estudio (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  consulta_id UUID REFERENCES consultas_medicas(id) ON DELETE CASCADE, paciente_id UUID NOT NULL REFERENCES pacientes(id), medico_id UUID REFERENCES medicos(id),
  tipo_estudio VARCHAR(40) NOT NULL, descripcion TEXT NOT NULL, indicaciones TEXT, fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, estado VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO'
);
CREATE INDEX idx_checkins_empresa_cita ON check_ins(empresa_id, cita_id);
CREATE INDEX idx_signos_empresa_paciente ON signos_vitales(empresa_id, paciente_id, fecha DESC);
CREATE INDEX idx_consultas_empresa_paciente ON consultas_medicas(empresa_id, paciente_id, creado_en DESC);
CREATE INDEX idx_antecedentes_empresa_paciente ON antecedentes_medicos(empresa_id, paciente_id);
CREATE INDEX idx_alergias_empresa_paciente ON alergias_paciente(empresa_id, paciente_id);
INSERT INTO permisos (codigo,nombre,descripcion) VALUES
 ('MENU_CHECKIN','Check-in','Registrar llegada de pacientes'),('MENU_SALA_ESPERA','Sala de espera','Gestionar sala de espera'),('MENU_SIGNOS_VITALES','Signos vitales','Registrar signos vitales'),('MENU_CONSULTA_MEDICA','Consulta médica','Gestionar consultas'),('MENU_EXPEDIENTE_CLINICO','Expediente clínico','Consultar expediente'),('MENU_ANTECEDENTES','Antecedentes','Gestionar antecedentes'),('MENU_ALERGIAS','Alergias','Gestionar alergias'),('MENU_DIAGNOSTICOS','Diagnósticos','Gestionar diagnósticos'),('MENU_TRATAMIENTOS','Tratamientos','Gestionar tratamientos'),('MENU_RECETAS','Recetas','Gestionar recetas'),('MENU_ORDENES_ESTUDIOS','Órdenes y estudios','Gestionar órdenes') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_permisos (rol_id, permiso_id) SELECT r.id,p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre='ADMINISTRADOR' ON CONFLICT DO NOTHING;
