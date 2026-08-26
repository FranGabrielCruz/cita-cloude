package com.citacloud.app.services;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagementControlExcelWriterTest {
    @Test
    void incluyePanelesDeControlYAlertasCentrados() throws Exception {
        var reporte = new ManagementControlReportService.Reporte(
                List.of(), List.of(), 0, 0, 0, 0, List.of(),
                List.of(new ManagementControlReportService.EspecialidadResumen("Cardiología", 5, 2, 25.0)),
                new ManagementControlReportService.TiemposAtencion("—", "—", "—"),
                List.of(new ManagementControlReportService.IndicadorControl("Asistencia", "90%", "≥ 85%", "Cumple")),
                List.of("✓ Asistencia dentro de la meta."));
        byte[] archivo = ManagementControlExcelWriter.generar(reporte, List.of(), "Clínica", new ManagementControlReportService.Filtros(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 23), null, null, null));

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            var hoja = libro.getSheet("Control y Alertas");
            assertNotNull(hoja);
            assertEquals("DISTRIBUCIÓN POR ESPECIALIDAD", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("ALERTAS Y OPORTUNIDADES", hoja.getRow(8).getCell(0).getStringCellValue());
            assertEquals(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER,
                    hoja.getRow(0).getCell(0).getCellStyle().getAlignment());
        }
    }
}
