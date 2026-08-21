package com.citacloud.app.services;

import com.citacloud.app.models.DetalleReceta;
import com.citacloud.app.models.Diagnostico;
import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Receta;
import com.citacloud.app.repositories.DetalleRecetaRepository;
import com.citacloud.app.repositories.DiagnosticoRepository;
import com.citacloud.app.repositories.EmpresaRepository;
import com.citacloud.app.repositories.PacienteRepository;
import com.citacloud.app.repositories.RecetaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class RecetaService {
    private final RecetaRepository recetas;
    private final DetalleRecetaRepository detalles;
    private final PacienteRepository pacientes;
    private final DiagnosticoRepository diagnosticos;
    private final EmpresaRepository empresas;
    private final JdbcTemplate jdbc;
    private final JasperPdfService jasper;

    public RecetaService(RecetaRepository recetas, DetalleRecetaRepository detalles, PacienteRepository pacientes,
                         DiagnosticoRepository diagnosticos, EmpresaRepository empresas, JdbcTemplate jdbc, JasperPdfService jasper) {
        this.recetas = recetas; this.detalles = detalles; this.pacientes = pacientes;
        this.diagnosticos = diagnosticos; this.empresas = empresas; this.jdbc = jdbc; this.jasper = jasper;
    }

    public List<Receta> listar(UUID empresaId, UUID pacienteId) { return recetas.findByEmpresaIdAndPacienteIdOrderByCreadoEnDesc(empresaId, pacienteId); }
    public List<DetalleReceta> medicamentos(UUID recetaId) { return detalles.findByRecetaId(recetaId); }
    public byte[] generarPdfJasper(UUID empresaId, UUID recetaId) { Receta r=buscar(empresaId,recetaId); var paciente=pacientes.findByIdAndEmpresaId(r.getPacienteId(),empresaId).orElseThrow(); Map<String,Object> p=new HashMap<>();p.put("CLINICA",empresas.findById(empresaId).map(Empresa::getNombre).orElse("CLÍNICA MÉDICA"));p.put("NUMERO",valor(r.getNumero()));p.put("FECHA",r.getEmitidoEn()==null?"-":r.getEmitidoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));p.put("DIAGNOSTICO",r.getDiagnosticoId()==null?"-":diagnosticos.findById(r.getDiagnosticoId()).map(Diagnostico::getDescripcion).orElse("-"));p.put("PACIENTE",valor(paciente.getNombreCompleto()));p.put("CEDULA",valor(paciente.getDocumento()));p.put("INDICACIONES",valor(r.getIndicacionesGenerales()));p.put("OBSERVACIONES",valor(r.getObservaciones()));List<Map<String,Object>> filas=new ArrayList<>();for(DetalleReceta d:medicamentos(recetaId)){Map<String,Object> f=new HashMap<>();f.put("medicamento",valor(d.getMedicamento()));f.put("forma",valor(d.getFormaFarmaceutica()));f.put("dosis",valor(d.getDosis()));f.put("via",valor(d.getViaAdministracion()));f.put("frecuencia",valor(d.getFrecuencia()));f.put("duracion",valor(d.getDuracion())+" Cant.: "+valor(d.getCantidad()));filas.add(f);}return jasper.generar("receta-medica",p,filas);}
    @Transactional public void cancelar(UUID empresaId, UUID recetaId, UUID usuarioId) { Receta receta = buscar(empresaId, recetaId); if ("ANULADA".equals(receta.getEstado())) throw new IllegalArgumentException("La receta ya está anulada."); receta.anular(usuarioId, "Cancelada desde consulta médica"); recetas.save(receta); }

    @Transactional
    public Receta crear(UUID empresaId, UUID pacienteId, UUID usuarioId, UUID diagnosticoId, String indicaciones, String observaciones,
                         java.time.LocalDate vigencia, String estado, List<Medicamento> medicamentos) {
        validar(empresaId, pacienteId, medicamentos);
        Receta receta = new Receta();
        receta.setEmpresaId(empresaId); receta.setPacienteId(pacienteId); receta.setCreadoPor(usuarioId); receta.setDiagnosticoId(diagnosticoId);
        receta.setIndicacionesGenerales(indicaciones); receta.setObservaciones(observaciones); receta.setVigenteHasta(vigencia);
        receta.setEstado(estado); receta.setCreadoEn(LocalDateTime.now()); receta.setActualizadoEn(LocalDateTime.now());
        receta = recetas.save(receta); guardar(receta, medicamentos);
        if ("EMITIDA".equals(estado)) emitir(receta);
        return receta;
    }

    @Transactional
    public void actualizar(UUID empresaId, UUID recetaId, UUID diagnosticoId, String indicaciones, String observaciones,
                           java.time.LocalDate vigencia, String estado, List<Medicamento> medicamentos) {
        Receta receta = buscar(empresaId, recetaId);
        if (!"BORRADOR".equals(receta.getEstado())) throw new IllegalArgumentException("La receta emitida no se puede modificar.");
        validar(empresaId, receta.getPacienteId(), medicamentos);
        detalles.deleteByRecetaId(recetaId);
        receta.setDiagnosticoId(diagnosticoId); receta.setIndicacionesGenerales(indicaciones); receta.setObservaciones(observaciones);
        receta.setVigenteHasta(vigencia); receta.setActualizadoEn(LocalDateTime.now()); recetas.save(receta); guardar(receta, medicamentos);
        if ("EMITIDA".equals(estado)) emitir(receta);
    }

    private void emitir(Receta receta) {
        if ("EMITIDA".equals(receta.getEstado()) && receta.getNumero() != null && receta.getEmitidoEn() != null) return;
        if (receta.getNumero() == null) {
            Integer consecutivo = jdbc.queryForObject("INSERT INTO contador_recetas(empresa_id,ultimo_numero) VALUES (?,1) ON CONFLICT(empresa_id) DO UPDATE SET ultimo_numero=contador_recetas.ultimo_numero+1 RETURNING ultimo_numero", Integer.class, receta.getEmpresaId());
            receta.setNumero("REC-" + java.time.Year.now() + "-" + String.format("%06d", consecutivo));
        }
        receta.setEstado("EMITIDA"); receta.setEmitidoEn(LocalDateTime.now()); receta.setSnapshot("Receta emitida: " + receta.getNumero()); recetas.save(receta);
    }

    public byte[] generarPdf(UUID empresaId, UUID recetaId) {
        Receta receta = buscar(empresaId, recetaId);
        if (!"EMITIDA".equals(receta.getEstado())) throw new IllegalArgumentException("La receta debe estar emitida.");
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(documento, salida); documento.open();
            Color azul = new Color(12, 54, 120), azulClaro = new Color(238, 244, 255), borde = new Color(180, 198, 225);
            String clinica = empresas.findById(empresaId).map(Empresa::getNombre).filter(nombre -> !nombre.isBlank()).orElse("CLINICA MEDICA");
            String diagnostico = receta.getDiagnosticoId() == null ? "Sin diagnostico asociado" : diagnosticos.findById(receta.getDiagnosticoId()).filter(dx -> empresaId.equals(dx.getEmpresaId())).map(dx -> (dx.getCodigo() == null ? "" : dx.getCodigo() + " - ") + dx.getDescripcion()).orElse("Sin diagnostico asociado");
            Paragraph nombreClinica = new Paragraph(clinica, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, azul)); nombreClinica.setSpacingAfter(8); documento.add(nombreClinica);
            Paragraph titulo = new Paragraph("RECETA MEDICA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 27, azul)); titulo.setSpacingAfter(8); documento.add(titulo);
            PdfPTable cabecera = new PdfPTable(new float[]{2, 1}); cabecera.setWidthPercentage(100); cabecera.setSpacingAfter(16);
            cabecera.addCell(infoCelda("DIAGNOSTICO RELACIONADO", diagnostico, azul, azulClaro, borde));
            cabecera.addCell(infoCelda("NUMERO", valor(receta.getNumero()) + "\n\nFECHA DE EMISION\n" + (receta.getEmitidoEn() == null ? "-" : receta.getEmitidoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))), azul, azulClaro, borde)); documento.add(cabecera);
            Paragraph tituloMedicamentos = new Paragraph("MEDICAMENTOS PRESCRITOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, azul)); tituloMedicamentos.setSpacingAfter(6); documento.add(tituloMedicamentos);
            PdfPTable tabla = new PdfPTable(new float[]{2.1f, 1.1f, 1, 1, 1.1f, 1.25f}); tabla.setWidthPercentage(100); tabla.setSpacingAfter(14);
            for (String encabezado : List.of("Medicamento", "Forma", "Dosis", "Via", "Frecuencia", "Duracion")) tabla.addCell(encabezadoCelda(encabezado, azul));
            for (DetalleReceta medicamento : medicamentos(recetaId)) {
                tabla.addCell(datosCelda(valor(medicamento.getMedicamento()) + "\n" + valor(medicamento.getConcentracion()), borde));
                tabla.addCell(datosCelda(valor(medicamento.getFormaFarmaceutica()), borde)); tabla.addCell(datosCelda(valor(medicamento.getDosis()), borde));
                tabla.addCell(datosCelda(valor(medicamento.getViaAdministracion()), borde)); tabla.addCell(datosCelda(valor(medicamento.getFrecuencia()), borde));
                tabla.addCell(datosCelda(valor(medicamento.getDuracion()) + "\nCant.: " + valor(medicamento.getCantidad()), borde));
            }
            documento.add(tabla);
            PdfPTable notas = new PdfPTable(2); notas.setWidthPercentage(100); notas.setSpacingAfter(32);
            notas.addCell(infoCelda("INDICACIONES GENERALES", valor(receta.getIndicacionesGenerales()), azul, azulClaro, borde));
            notas.addCell(infoCelda("OBSERVACIONES", valor(receta.getObservaciones()), azul, azulClaro, borde)); documento.add(notas);
            PdfPTable firma = new PdfPTable(new float[]{1, 1}); firma.setWidthPercentage(100);
            PdfPCell aviso = new PdfPCell(new Paragraph("Conserve esta receta para futuras consultas.", FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(80, 80, 80)))); aviso.setBorder(PdfPCell.NO_BORDER); aviso.setPaddingTop(24); firma.addCell(aviso);
            PdfPCell sello = new PdfPCell(new Paragraph("________________________________________\nFirma y sello del profesional", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, azul))); sello.setHorizontalAlignment(Element.ALIGN_CENTER); sello.setBorder(PdfPCell.NO_BORDER); sello.setPaddingTop(18); firma.addCell(sello); documento.add(firma);
            documento.close(); return salida.toByteArray();
        } catch (Exception ex) { throw new IllegalStateException("No se pudo generar el PDF.", ex); }
    }

    private PdfPCell encabezadoCelda(String texto, Color azul) { PdfPCell celda = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE))); celda.setPadding(9); celda.setBackgroundColor(azul); celda.setHorizontalAlignment(Element.ALIGN_CENTER); return celda; }
    private PdfPCell datosCelda(String texto, Color borde) { PdfPCell celda = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA, 9))); celda.setPadding(8); celda.setBorderColor(borde); return celda; }
    private PdfPCell infoCelda(String titulo, String texto, Color azul, Color fondo, Color borde) { PdfPCell celda = new PdfPCell(); celda.setPadding(12); celda.setBackgroundColor(fondo); celda.setBorderColor(borde); celda.addElement(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azul))); celda.addElement(new Paragraph(valor(texto), FontFactory.getFont(FontFactory.HELVETICA, 10))); return celda; }
    private String valor(String texto) { return texto == null || texto.isBlank() ? "-" : texto; }

    private void guardar(Receta receta, List<Medicamento> medicamentos) { for (Medicamento medicamento : medicamentos) { DetalleReceta detalle = new DetalleReceta(); detalle.setRecetaId(receta.getId()); detalle.setMedicamento(medicamento.medicamento()); detalle.setPrincipioActivo(medicamento.principioActivo()); detalle.setConcentracion(medicamento.concentracion()); detalle.setFormaFarmaceutica(medicamento.formaFarmaceutica()); detalle.setDosis(medicamento.dosis()); detalle.setViaAdministracion(medicamento.via()); detalle.setFrecuencia(medicamento.frecuencia()); detalle.setDuracion(medicamento.duracion()); detalle.setCantidad(medicamento.cantidad()); detalle.setIndicaciones(medicamento.indicaciones()); detalles.save(detalle); } }
    private Receta buscar(UUID empresaId, UUID recetaId) { return recetas.findById(recetaId).filter(receta -> empresaId.equals(receta.getEmpresaId())).orElseThrow(() -> new IllegalArgumentException("Receta no encontrada.")); }
    private void validar(UUID empresaId, UUID pacienteId, List<Medicamento> medicamentos) { if (pacientes.findByIdAndEmpresaId(pacienteId, empresaId).isEmpty() || medicamentos == null || medicamentos.isEmpty()) throw new IllegalArgumentException("Paciente y medicamentos son obligatorios."); }
    public record Medicamento(String medicamento, String principioActivo, String concentracion, String formaFarmaceutica, String dosis, String via, String frecuencia, String duracion, String cantidad, String indicaciones) { }
}
