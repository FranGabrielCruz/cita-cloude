INSERT INTO contador_recetas (empresa_id, ultimo_numero)
SELECT empresa_id, COALESCE(MAX(CAST(substring(numero FROM '[0-9]+$') AS INTEGER)), 0)
FROM recetas
WHERE numero IS NOT NULL
GROUP BY empresa_id
ON CONFLICT (empresa_id) DO UPDATE
SET ultimo_numero = GREATEST(contador_recetas.ultimo_numero, EXCLUDED.ultimo_numero);
