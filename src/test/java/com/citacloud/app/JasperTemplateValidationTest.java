package com.citacloud.app;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JasperTemplateValidationTest {
    @Test
    void compilaYGeneraPlantillaDeReceta() {
        assertDoesNotThrow(() -> {
            try (var receta = new ClassPathResource("reportes/receta-medica.jrxml").getInputStream()) {
                var reporte = JasperCompileManager.compileReport(receta);
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("CLINICA", "Clínica San Rafael"); parametros.put("NUMERO", "REC-2026-000001");
                parametros.put("FECHA", "20/08/2026 11:15"); parametros.put("DIAGNOSTICO", "Hipertensión esencial");
                parametros.put("PACIENTE", "Juan Pérez Rodríguez"); parametros.put("CEDULA", "001-1234567-8");
                parametros.put("INDICACIONES", "Tomar después de alimentos"); parametros.put("OBSERVACIONES", "Control en 30 días");
                List<Map<String, ?>> filas = List.of(Map.of("medicamento", "Losartán", "forma", "Tableta", "dosis", "1 tableta",
                        "via", "Oral", "frecuencia", "Cada 24 h", "duracion", "30 días · Cant.: 30"));
                var documento = JasperFillManager.fillReport(reporte, parametros, new JRMapCollectionDataSource(filas));
                JasperExportManager.exportReportToPdf(documento);
            }
        });
    }

    @Test
    void compilaPlantillaDeOrden() {
        assertDoesNotThrow(() -> {
            try (var orden = new ClassPathResource("reportes/orden-medica.jrxml").getInputStream()) {
                JasperCompileManager.compileReport(orden);
            }
        });
    }
}
