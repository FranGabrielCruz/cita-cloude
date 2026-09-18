-- Los códigos descriptivos de auditoría pueden superar el límite histórico de 40 caracteres.
ALTER TABLE auditoria
    ALTER COLUMN accion TYPE VARCHAR(80);
