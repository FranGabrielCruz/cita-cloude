CREATE TABLE IF NOT EXISTS contador_recetas (
    empresa_id UUID PRIMARY KEY REFERENCES empresas(id) ON DELETE CASCADE,
    ultimo_numero INTEGER NOT NULL DEFAULT 0
);
