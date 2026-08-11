-- Corrige el hash del usuario demostrativo si se sustituyo por uno que no corresponde a admin123.
-- Se limita estrictamente al administrador inicial de la empresa de ejemplo.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE usuarios
SET password_hash = crypt('admin123', gen_salt('bf', 10))
WHERE usuario = 'admin'
  AND empresa_id = (
      SELECT id
      FROM empresas
      WHERE codigo = 'CLINICA01'
  );
