package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Generador técnico cargado al exportar, no durante la creación de beans Spring. */
final class ManagementControlExcelWriter {
    private static final int COLUMNAS_CONTROL = 6;

    private ManagementControlExcelWriter() { }

    static byte[] generar(ManagementControlReportService.Reporte reporte, List<Cita> citas, String clinica,
                          ManagementControlReportService.Filtros filtros) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            resumen(libro, reporte, clinica, filtros, estilos);
            citas(libro, citas, estilos);
            productividad(libro, reporte, estilos);
            controlYAlertas(libro, reporte, estilos);
            vacia(libro, "Tiempos", new String[]{"Fecha", "Médico", "Paciente", "Check-in", "Inicio consulta", "Fin consulta", "Espera", "Duración", "Tiempo total"}, estilos);
            libro.write(salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel de gestión y control.", e);
        }
    }

    private static void resumen(Workbook libro, ManagementControlReportService.Reporte reporte, String clinica,
                                ManagementControlReportService.Filtros filtros, Estilos estilos) {
        Sheet hoja = libro.createSheet("Resumen");
        hoja.createRow(0).createCell(0).setCellValue("CITA CLOUD · REPORTE DE GESTIÓN Y CONTROL");
        hoja.createRow(1).createCell(0).setCellValue("Clínica: " + clinica);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        hoja.createRow(2).createCell(0).setCellValue("Período: " + formato.format(filtros.desde()) + " - " + formato.format(filtros.hasta()));
        String[] encabezados = {"Indicador", "Resultado", "Variación"};
        cabecera(hoja, 4, encabezados, estilos.encabezadoAzul);
        int fila = 5;
        for (var kpi : reporte.resumen()) {
            Row registro = hoja.createRow(fila++);
            registro.createCell(0).setCellValue(kpi.nombre());
            registro.createCell(1).setCellValue(kpi.valor());
            variacion(registro.createCell(2), kpi.variacion());
        }
        terminar(hoja, 4, fila - 1, encabezados.length);
    }

    private static void citas(Workbook libro, List<Cita> datos, Estilos estilos) {
        Sheet hoja = libro.createSheet("Citas");
        String[] encabezados = {"Fecha", "Hora", "No. cita", "Paciente", "Médico", "Especialidad", "Sucursal", "Estado"};
        cabecera(hoja, 0, encabezados, estilos.encabezadoAzul);
        int fila = 1;
        for (Cita cita : datos) {
            Row registro = hoja.createRow(fila++);
            registro.createCell(0).setCellValue(cita.getFecha());
            registro.createCell(1).setCellValue(cita.getHoraInicio().toString());
            registro.createCell(2).setCellValue(cita.getId().toString());
            registro.createCell(3).setCellValue(cita.getPaciente().getNombreCompleto());
            registro.createCell(4).setCellValue(cita.getMedico().getNombreCompleto());
            registro.createCell(5).setCellValue(cita.getMedico().getEspecialidadesTexto());
            registro.createCell(6).setCellValue(cita.getSucursal().getNombre());
            registro.createCell(7).setCellValue(cita.getEstado());
        }
        terminar(hoja, 0, fila - 1, encabezados.length);
    }

    private static void productividad(Workbook libro, ManagementControlReportService.Reporte reporte, Estilos estilos) {
        Sheet hoja = libro.createSheet("Productividad Médica");
        String[] encabezados = {"Médico", "Citas", "Atendidos", "Canceladas", "No-show", "Promedio por día"};
        cabecera(hoja, 0, encabezados, estilos.encabezadoAzul);
        int fila = 1;
        for (var productividad : reporte.productividad()) {
            Row registro = hoja.createRow(fila++);
            registro.createCell(0).setCellValue(productividad.medico());
            registro.createCell(1).setCellValue(productividad.citas());
            registro.createCell(2).setCellValue(productividad.atendidas());
            registro.createCell(3).setCellValue(productividad.canceladas());
            registro.createCell(4).setCellValue(productividad.noShow());
            registro.createCell(5).setCellValue(productividad.promedioDia());
        }
        terminar(hoja, 0, fila - 1, encabezados.length);
    }

    /** Replica en Excel los tres paneles finales del PDF, con títulos y datos centrados. */
    private static void controlYAlertas(Workbook libro, ManagementControlReportService.Reporte reporte, Estilos estilos) {
        Sheet hoja = libro.createSheet("Control y Alertas");
        for (int columna = 0; columna < COLUMNAS_CONTROL; columna++) hoja.setColumnWidth(columna, 19 * 256);
        int fila = 0;

        tituloPanel(hoja, fila++, "DISTRIBUCIÓN POR ESPECIALIDAD", estilos.panelAzul);
        String[] especialidades = {"Especialidad", "Citas", "Atendidos", "Porcentaje"};
        cabecera(hoja, fila++, especialidades, estilos.encabezadoAzulClaro);
        if (reporte.especialidades().isEmpty()) {
            mensajePanel(hoja, fila++, "Sin citas registradas para el período seleccionado.", estilos.datoAzul);
        } else {
            for (var especialidad : reporte.especialidades()) {
                Row registro = hoja.createRow(fila++);
                valorCentrado(registro.createCell(0), especialidad.especialidad(), estilos.datoAzul);
                valorCentrado(registro.createCell(1), especialidad.citas(), estilos.datoAzul);
                valorCentrado(registro.createCell(2), especialidad.atendidas(), estilos.datoAzul);
                valorCentrado(registro.createCell(3), especialidad.porcentaje() / 100d, estilos.porcentajeAzul);
            }
        }

        fila++;
        tituloPanel(hoja, fila++, "INDICADORES DE CONTROL", estilos.panelAzul);
        String[] indicadores = {"Indicador", "Resultado", "Meta", "Estado"};
        cabecera(hoja, fila++, indicadores, estilos.encabezadoAzulClaro);
        for (var indicador : reporte.indicadores()) {
            Row registro = hoja.createRow(fila++);
            valorCentrado(registro.createCell(0), indicador.indicador(), estilos.datoAzul);
            valorCentrado(registro.createCell(1), indicador.resultado(), estilos.datoAzul);
            valorCentrado(registro.createCell(2), indicador.meta(), estilos.datoAzul);
            valorCentrado(registro.createCell(3), indicador.estado(), "Cumple".equals(indicador.estado()) ? estilos.estadoCumple : estilos.estadoAtencion);
        }

        fila++;
        tituloPanel(hoja, fila++, "ALERTAS Y OPORTUNIDADES", estilos.panelAlerta);
        List<String> alertas = reporte.alertas().isEmpty()
                ? List.of("✓ Sin alertas críticas durante el período seleccionado.") : reporte.alertas();
        for (String alerta : alertas) mensajePanel(hoja, fila++, alerta, estilos.datoAlerta);
        hoja.createFreezePane(0, 1);
    }

    private static void vacia(Workbook libro, String nombre, String[] encabezados, Estilos estilos) {
        Sheet hoja = libro.createSheet(nombre);
        cabecera(hoja, 0, encabezados, estilos.encabezadoAzul);
        terminar(hoja, 0, 0, encabezados.length);
    }

    private static void tituloPanel(Sheet hoja, int fila, String titulo, CellStyle estilo) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, COLUMNAS_CONTROL - 1));
        Row registro = hoja.createRow(fila);
        Cell celda = registro.createCell(0);
        celda.setCellValue(titulo);
        celda.setCellStyle(estilo);
        registro.setHeightInPoints(23);
    }

    private static void mensajePanel(Sheet hoja, int fila, String mensaje, CellStyle estilo) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, COLUMNAS_CONTROL - 1));
        Row registro = hoja.createRow(fila);
        Cell celda = registro.createCell(0);
        celda.setCellValue(mensaje);
        celda.setCellStyle(estilo);
        registro.setHeightInPoints(21);
    }

    private static void cabecera(Sheet hoja, int fila, String[] encabezados, CellStyle estilo) {
        Row registro = hoja.createRow(fila);
        for (int columna = 0; columna < encabezados.length; columna++) {
            Cell celda = registro.createCell(columna);
            celda.setCellValue(encabezados[columna]);
            celda.setCellStyle(estilo);
        }
    }

    private static void terminar(Sheet hoja, int filaEncabezado, int ultimaFila, int columnas) {
        hoja.setAutoFilter(new CellRangeAddress(filaEncabezado, Math.max(filaEncabezado, ultimaFila), 0, columnas - 1));
        hoja.createFreezePane(0, filaEncabezado + 1);
        for (int columna = 0; columna < columnas; columna++) hoja.setColumnWidth(columna, 20 * 256);
    }

    private static void valorCentrado(Cell celda, String valor, CellStyle estilo) { celda.setCellValue(valor); celda.setCellStyle(estilo); }
    private static void valorCentrado(Cell celda, double valor, CellStyle estilo) { celda.setCellValue(valor); celda.setCellStyle(estilo); }
    private static void valorCentrado(Cell celda, long valor, CellStyle estilo) { celda.setCellValue(valor); celda.setCellStyle(estilo); }
    private static void variacion(Cell celda, Double valor) { if (valor == null) celda.setCellValue("—"); else celda.setCellValue(valor / 100d); }

    private static final class Estilos {
        private final CellStyle encabezadoAzul;
        private final CellStyle panelAzul;
        private final CellStyle encabezadoAzulClaro;
        private final CellStyle datoAzul;
        private final CellStyle porcentajeAzul;
        private final CellStyle panelAlerta;
        private final CellStyle datoAlerta;
        private final CellStyle estadoCumple;
        private final CellStyle estadoAtencion;

        private Estilos(Workbook libro) {
            encabezadoAzul = estilo(libro, IndexedColors.DARK_BLUE, IndexedColors.WHITE, true, HorizontalAlignment.CENTER);
            panelAzul = estilo(libro, IndexedColors.PALE_BLUE, IndexedColors.DARK_BLUE, true, HorizontalAlignment.CENTER);
            encabezadoAzulClaro = estilo(libro, IndexedColors.LIGHT_CORNFLOWER_BLUE, IndexedColors.DARK_BLUE, true, HorizontalAlignment.CENTER);
            datoAzul = estilo(libro, IndexedColors.PALE_BLUE, IndexedColors.BLACK, false, HorizontalAlignment.CENTER);
            porcentajeAzul = estilo(libro, IndexedColors.PALE_BLUE, IndexedColors.BLACK, false, HorizontalAlignment.CENTER);
            porcentajeAzul.setDataFormat(libro.createDataFormat().getFormat("0.0%"));
            panelAlerta = estilo(libro, IndexedColors.LIGHT_YELLOW, IndexedColors.DARK_YELLOW, true, HorizontalAlignment.CENTER);
            datoAlerta = estilo(libro, IndexedColors.LEMON_CHIFFON, IndexedColors.BLACK, false, HorizontalAlignment.CENTER);
            estadoCumple = estilo(libro, IndexedColors.LIGHT_GREEN, IndexedColors.DARK_GREEN, true, HorizontalAlignment.CENTER);
            estadoAtencion = estilo(libro, IndexedColors.LIGHT_ORANGE, IndexedColors.DARK_RED, true, HorizontalAlignment.CENTER);
        }

        private static CellStyle estilo(Workbook libro, IndexedColors fondo, IndexedColors texto, boolean negrita, HorizontalAlignment alineacion) {
            CellStyle estilo = libro.createCellStyle();
            estilo.setFillForegroundColor(fondo.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estilo.setAlignment(alineacion);
            estilo.setVerticalAlignment(VerticalAlignment.CENTER);
            estilo.setBorderTop(BorderStyle.THIN); estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN); estilo.setBorderRight(BorderStyle.THIN);
            Font fuente = libro.createFont();
            fuente.setColor(texto.getIndex()); fuente.setBold(negrita);
            estilo.setFont(fuente);
            return estilo;
        }
    }
}
