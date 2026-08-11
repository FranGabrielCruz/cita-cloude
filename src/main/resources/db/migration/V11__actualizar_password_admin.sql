-- Actualizar la contraseña del usuario admin por defecto a admin123
UPDATE usuarios
SET password_hash = 'admin123'
WHERE usuario = 'admin';
