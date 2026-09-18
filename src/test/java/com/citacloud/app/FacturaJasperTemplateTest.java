package com.citacloud.app;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FacturaJasperTemplateTest {
    @Test void generaFacturaFormalDeEjemplo() {
        assertDoesNotThrow(() -> {
            try (var plantilla = new ClassPathResource("reportes/factura.jrxml").getInputStream()) {
                var reporte = JasperCompileManager.compileReport(plantilla); Map<String,Object> p = new HashMap<>();
                p.put("CLINICA","Clínica San Rafael"); p.put("RNC","131-12345-6"); p.put("DIRECCION","Av. Principal 123"); p.put("TELEFONO","809-555-0100"); p.put("SUCURSAL","Sede central"); p.put("CAJA","Caja principal"); p.put("TURNO","CJ-000125"); p.put("NUMERO","FAC-000125"); p.put("FECHA","02/09/2026"); p.put("ESTADO","PAGADA"); p.put("PACIENTE","Ana Pérez"); p.put("EXPEDIENTE","HC-0000125"); p.put("DOCUMENTO","001-1234567-8"); p.put("MEDICO","Dr. Juan Pérez"); p.put("COMPROBANTE","Consumidor final · B0200000125"); p.put("SUBTOTAL","3,800.00"); p.put("DESCUENTO","100.00"); p.put("IMPUESTOS","486.00"); p.put("TOTAL","4,186.00"); p.put("PAGADO","4,186.00"); p.put("PENDIENTE","0.00"); p.put("PAGOS","PAG-000251 · 02/09/2026 03:15 PM · EFECTIVO · RD$ 4,186.00"); p.put("OBSERVACION","Documento de ejemplo para validación visual.");
                List<Map<String,?>> lineas = List.of(Map.of("descripcion","Consulta Cardiología","tipo","SERVICIO","cantidad","1.00","precio","2,500.00","descuento","100.00","impuesto","432.00","total","2,832.00"), Map.of("descripcion","Hemograma completo","tipo","SERVICIO","cantidad","1.00","precio","800.00","descuento","0.00","impuesto","0.00","total","800.00"), Map.of("descripcion","Medicamento X","tipo","PRODUCTO","cantidad","2.00","precio","250.00","descuento","0.00","impuesto","54.00","total","554.00"));
                byte[] pdf = JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(reporte, p, new JRMapCollectionDataSource(lineas)));
                assertTrue(pdf.length > 2_000); Path salida = Path.of("output", "pdf", "factura-ejemplo.pdf"); Files.createDirectories(salida.getParent()); Files.write(salida, pdf);
            }
        });
    }
}
