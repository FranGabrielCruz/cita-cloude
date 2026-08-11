CREATE TABLE tipos_cita (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    duracion_minutos INT NOT NULL DEFAULT 30,
    precio NUMERIC(12,2) DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (empresa_id, nombre)
);

CREATE TABLE citas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    paciente_id UUID NOT NULL REFERENCES pacientes(id) ON DELETE RESTRICT,
    medico_id UUID NOT NULL REFERENCES medicos(id) ON DELETE RESTRICT,
    sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE RESTRICT,
    consultorio_id UUID REFERENCES consultorios(id) ON DELETE SET NULL,
    tipo_cita_id UUID REFERENCES tipos_cita(id) ON DELETE SET NULL,
    seguro_paciente_id UUID REFERENCES seguros_paciente(id) ON DELETE SET NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    motivo TEXT,
    notas TEXT,
    creado_por UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE historial_cita (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    cita_id UUID NOT NULL REFERENCES citas(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    observacion TEXT,
    usuario_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_citas_empresa_fecha ON citas(empresa_id, fecha);
CREATE INDEX idx_citas_empresa_medico ON citas(empresa_id, medico_id, fecha);

-- Datos iniciales (Tenant: CLINICA01)
INSERT INTO empresas (id, codigo, nombre, rnc_identificacion, telefono, email, direccion)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'CLINICA01', 'Clínica San Rafael', '101-99887-1', '(809) 555-0000', 'contacto@sanrafael.com', 'Av. Independencia #102, Santo Domingo');

-- Usuario admin (Contraseña: admin123)
-- Hash BCrypt para 'admin123': $2a$10$rD7P5E0gX0j.f8c2e6X8hO1.8e9r9O1k0a1b2c3d4e5f6g7h8i9j
INSERT INTO usuarios (id, empresa_id, usuario, password_hash, nombre, apellido, email, telefono, activo)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', '$2a$10$3pS8D0tK4O1w8c1W9.5y5u6Q0g1h2i3j4k5l6m7n8o9p0q1r2s3t4', 'Gabriel', 'Martínez', 'admin@sanrafael.com', '(809) 555-0101', true);

INSERT INTO roles (id, empresa_id, nombre, descripcion)
VALUES ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ADMINISTRADOR', 'Acceso completo al sistema de la clínica');

INSERT INTO usuario_roles (usuario_id, rol_id)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33');

INSERT INTO sucursales (id, empresa_id, codigo, nombre, telefono, direccion)
VALUES ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PRINCIPAL', 'Clínica San Rafael - Central', '(809) 555-0123', 'Av. 27 de Febrero #45');

INSERT INTO especialidades (id, empresa_id, nombre, descripcion) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Cardiología', 'Enfermedades del corazón y aparato circulatorio'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Pediatría', 'Atención médica a recién nacidos, niños y adolescentes'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Medicina General', 'Atención primaria y prevención de enfermedades'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Dermatología', 'Enfermedades de la piel, cabello y uñas');

INSERT INTO medicos (id, empresa_id, codigo, nombre, apellido, exequatur, telefono, email) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MED001', 'Dr. Carlos', 'Ruiz', 'EX-12345', '(809) 555-9871', 'cruiz@sanrafael.com'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MED002', 'Dra. Elena', 'Silva', 'EX-23456', '(809) 555-9872', 'esilva@sanrafael.com'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MED003', 'Dr. Juan', 'Pérez', 'EX-34567', '(809) 555-9873', 'jperez@sanrafael.com'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MED004', 'Dra. Laura', 'Montes', 'EX-45678', '(809) 555-9874', 'lmontes@sanrafael.com');

INSERT INTO medico_especialidades (medico_id, especialidad_id) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54');

INSERT INTO pacientes (id, empresa_id, documento, tipo_documento, nombre, apellido, telefono, email) VALUES
('11eebc99-9c0b-4ef8-bb6d-6bb9bd380a71', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '001-2345678-9', 'CEDULA', 'González', 'Pérez', '(809) 555-0123', 'gperez@email.com'),
('11eebc99-9c0b-4ef8-bb6d-6bb9bd380a72', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '402-9876543-2', 'CEDULA', 'Eduardo', 'Martínez', '(829) 555-0987', 'emartinez@email.com'),
('11eebc99-9c0b-4ef8-bb6d-6bb9bd380a73', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '001-5555555-5', 'CEDULA', 'Rodríguez', 'Sosa', '(809) 555-4433', 'rsosa@email.com');

INSERT INTO consultorios (id, empresa_id, sucursal_id, codigo, nombre, ubicacion) VALUES
('22eebc99-9c0b-4ef8-bb6d-6bb9bd380a81', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'CONS01', 'Consultorio 1', 'Primer Piso - Ala Norte'),
('22eebc99-9c0b-4ef8-bb6d-6bb9bd380a82', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'CONS02', 'Consultorio 2', 'Primer Piso - Ala Sur');

INSERT INTO aseguradoras (id, empresa_id, nombre, rnc, telefono) VALUES
('33eebc99-9c0b-4ef8-bb6d-6bb9bd380a91', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ARS Humano', '101-00001-1', '(809) 555-1111'),
('33eebc99-9c0b-4ef8-bb6d-6bb9bd380a92', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MAPFRE BHD', '101-00002-2', '(809) 555-2222');

INSERT INTO seguros_paciente (id, empresa_id, paciente_id, aseguradora_id, numero_poliza) VALUES
('44eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a71', '33eebc99-9c0b-4ef8-bb6d-6bb9bd380a91', 'POL-998877'),
('44eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a72', '33eebc99-9c0b-4ef8-bb6d-6bb9bd380a92', 'POL-665544');

INSERT INTO tipos_cita (id, empresa_id, nombre, duracion_minutos, precio) VALUES
('55eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Consulta General', 30, 2500.00),
('55eebc99-9c0b-4ef8-bb6d-6bb9bd380c02', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Seguimiento', 20, 1500.00),
('55eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Vacunación', 15, 800.00);

INSERT INTO citas (id, empresa_id, paciente_id, medico_id, sucursal_id, consultorio_id, tipo_cita_id, fecha, hora_inicio, hora_fin, estado, motivo) VALUES
('66eebc99-9c0b-4ef8-bb6d-6bb9bd380d01', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a71', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a81', '55eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', CURRENT_DATE, '08:00:00', '08:30:00', 'CONFIRMADA', 'Chequeo rutinario de presión arterial'),
('66eebc99-9c0b-4ef8-bb6d-6bb9bd380d02', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a72', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a82', '55eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', CURRENT_DATE, '09:00:00', '09:30:00', 'CONFIRMADA', 'Vacunación pediátrica'),
('66eebc99-9c0b-4ef8-bb6d-6bb9bd380d03', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a73', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a81', '55eebc99-9c0b-4ef8-bb6d-6bb9bd380c01', CURRENT_DATE, '10:00:00', '10:30:00', 'PENDIENTE', 'Consulta por malestar general');
