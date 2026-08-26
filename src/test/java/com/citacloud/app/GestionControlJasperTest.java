package com.citacloud.app;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestionControlJasperTest {
    @Test void plantillaGestionControlCompila() {
        assertDoesNotThrow(() -> JasperCompileManager.compileReport(new ClassPathResource("reportes/gestion_control.jrxml").getInputStream()));
    }

    @Test void plantillaGestionControlRenderizaValoresDelResumen() throws Exception {
        JasperReport plantilla = JasperCompileManager.compileReport(
                new ClassPathResource("reportes/gestion_control.jrxml").getInputStream());
        Map<String, Object> parametros = new HashMap<>(Map.ofEntries(
                Map.entry("CLINICA", "Clínica de prueba"), Map.entry("PERIODO", "2026-08-01 - 2026-08-23"),
                Map.entry("TOTAL_CITAS", 29L), Map.entry("ATENDIDOS", 14L), Map.entry("NUEVOS", 8L),
                Map.entry("CANCELADAS", "5"), Map.entry("NO_SHOW", "2"), Map.entry("ASISTENCIA", "48.3%"),
                Map.entry("ESTADOS", "Atendidas: 14 | Confirmadas: 8"), Map.entry("ESPECIALIDADES", "Pediatría: 75.9%"),
                Map.entry("INDICADORES", "Tasa de asistencia 100% · Cumple"), Map.entry("ALERTAS", "Sin alertas"),
                Map.entry("GENERADO", "2026-08-23")));
        var datos = new JRMapCollectionDataSource(List.of(Map.of("medico", "Dra. Ejemplo", "citas", 5L,
                "atendidos", 4L, "canceladas", 0L, "noShow", 0L, "promedio", 1.2D)));
        byte[] pdf = JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(plantilla, parametros, datos));
        assertTrue(pdf.length > 1_000, "El PDF generado debe contener contenido");
    }
}
