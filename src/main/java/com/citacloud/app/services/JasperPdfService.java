package com.citacloud.app.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class JasperPdfService {
    private final MeterRegistry metricas;

    public JasperPdfService(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    public byte[] generar(String plantilla, Map<String, Object> parametros, List<Map<String, Object>> filas) {
        String tipo = tipoControlado(plantilla);
        Timer.Sample muestra = Timer.start(metricas);
        try (InputStream entrada = new ClassPathResource("reportes/" + plantilla + ".jrxml").getInputStream()) {
            Collection<Map<String, ?>> datos = new ArrayList<>(filas);
            byte[] pdf = JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(
                    JasperCompileManager.compileReport(entrada), parametros, new JRMapCollectionDataSource(datos)));
            Counter.builder("pdf.generated").tag("type", tipo).register(metricas).increment();
            return pdf;
        } catch (Exception exception) {
            Counter.builder("pdf.failed").tag("type", tipo).register(metricas).increment();
            throw new IllegalStateException("No se pudo generar el reporte Jasper.", exception);
        } finally {
            muestra.stop(Timer.builder("pdf.generation.duration").tag("type", tipo)
                    .publishPercentileHistogram().register(metricas));
        }
    }

    private String tipoControlado(String plantilla) {
        return switch (plantilla) {
            case "factura" -> "invoice";
            case "recibo-pago" -> "payment_receipt";
            case "cierre-caja" -> "cash_closure";
            case "gestion_control" -> "financial_report";
            case "orden-medica" -> "medical_order";
            case "receta-medica" -> "prescription";
            default -> "other";
        };
    }
}
