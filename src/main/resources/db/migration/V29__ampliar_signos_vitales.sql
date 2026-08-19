ALTER TABLE signos_vitales
    ADD COLUMN IF NOT EXISTS glucemia NUMERIC(7,2),
    ADD COLUMN IF NOT EXISTS circunferencia_abdominal NUMERIC(7,2),
    ADD COLUMN IF NOT EXISTS nivel_dolor INTEGER;
