ALTER TABLE configuracion_notificaciones_cita
    ADD COLUMN smtp_from VARCHAR(255),
    ADD COLUMN smtp_username VARCHAR(255);

COMMENT ON COLUMN configuracion_notificaciones_cita.smtp_from IS 'Dirección remitente configurada por la clínica';
COMMENT ON COLUMN configuracion_notificaciones_cita.smtp_username IS 'Usuario SMTP configurado por la clínica; la contraseña permanece en la configuración segura del servidor';
