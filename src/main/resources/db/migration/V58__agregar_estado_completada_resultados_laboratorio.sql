ALTER TABLE resultados_laboratorio
    DROP CONSTRAINT chk_resultado_laboratorio_estado;

ALTER TABLE resultados_laboratorio
    ADD CONSTRAINT chk_resultado_laboratorio_estado
    CHECK (estado IN ('BORRADOR', 'POR_VALIDAR', 'VALIDADO', 'COMPLETADA', 'CORREGIDO'));
