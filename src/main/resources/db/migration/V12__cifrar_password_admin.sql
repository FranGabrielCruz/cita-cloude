-- Sustituye la contraseÃ±a de demostraciÃ³n heredada en texto plano por un hash BCrypt.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE usuarios
SET password_hash = crypt('admin123', gen_salt('bf', 10))
WHERE usuario = 'admin'
  AND password_hash = 'admin123';
