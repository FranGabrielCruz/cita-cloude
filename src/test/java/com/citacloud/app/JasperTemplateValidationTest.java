package com.citacloud.app;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void generaReciboDePagoConContenido() {
        assertDoesNotThrow(() -> {
            try (var recibo = new ClassPathResource("reportes/recibo-pago.jrxml").getInputStream()) {
                var reporte = JasperCompileManager.compileReport(recibo);
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("CLINICA", "Clínica San Rafael"); parametros.put("RNC", "131-12345-6"); parametros.put("DIRECCION", "Av. Principal 123"); parametros.put("TELEFONO", "809-555-0100"); parametros.put("SUCURSAL", "Sede central"); parametros.put("NUMERO", "PAG-000123");
                parametros.put("FECHA", "24/08/2026 · 3:30 PM"); parametros.put("PACIENTE", "Francisco Puello"); parametros.put("EXPEDIENTE", "HC-0000005"); parametros.put("DOCUMENTO", "****1234"); parametros.put("ESTADO", "APLICADO");
                parametros.put("METODO", "EFECTIVO"); parametros.put("REFERENCIA", ""); parametros.put("EFECTIVO", "2,500.00"); parametros.put("CAMBIO", "0.00"); parametros.put("RECIBIDO_POR", "Administrador"); parametros.put("TOTAL_CARGOS", "2,500.00"); parametros.put("PAGADO_ANTERIOR", "0.00"); parametros.put("ESTE_PAGO", "2,500.00"); parametros.put("SALDO_PENDIENTE", "0.00"); parametros.put("REEMBOLSADO", "0.00"); parametros.put("NOTA", "Pago de prueba");
                List<Map<String, ?>> filas = List.of(Map.of("concepto", "Consulta médica", "referencia", "FAC-000123", "monto", "2,500.00"));
                var documento = JasperFillManager.fillReport(reporte, parametros, new JRMapCollectionDataSource(filas));
                byte[] pdf = JasperExportManager.exportReportToPdf(documento);
                assertTrue(pdf.length > 1_000, "El recibo PDF debe contener contenido renderizado.");
                Files.write(Path.of("target", "recibo-pago-prueba.pdf"), pdf);
            }
        });
    }

    @Test
    void compilaPlantillaDeCierreDeCaja() {
        assertDoesNotThrow(() -> {
            try (var cierre = new ClassPathResource("reportes/cierre-caja.jrxml").getInputStream()) {
                var reporte = JasperCompileManager.compileReport(cierre);
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("CLINICA", "Clínica San Rafael"); parametros.put("RNC", "101-99887-1");
                parametros.put("SUCURSAL", "Central"); parametros.put("NUMERO", "CJ-000001");
                parametros.put("APERTURA", "25/08/2026 08:00"); parametros.put("CIERRE", "25/08/2026 17:00");
                parametros.put("FONDO", new java.math.BigDecimal("5000.00")); parametros.put("INGRESOS", new java.math.BigDecimal("3500.00"));
                parametros.put("EGRESOS", new java.math.BigDecimal("500.00")); parametros.put("ESPERADO", new java.math.BigDecimal("8000.00"));
                parametros.put("CONTADO", new java.math.BigDecimal("8000.00")); parametros.put("DIFERENCIA", java.math.BigDecimal.ZERO); parametros.put("MOTIVO", "");
                var documento = JasperFillManager.fillReport(reporte, parametros, new JRMapCollectionDataSource(List.of(Map.of("hora", "10:00", "tipo", "PAYMENT", "referencia", "PAG-000001", "metodo", "EFECTIVO", "entrada", new java.math.BigDecimal("100.00"), "salida", java.math.BigDecimal.ZERO))));
                assertTrue(JasperExportManager.exportReportToPdf(documento).length > 1_000);
            }
        });
    }
}
