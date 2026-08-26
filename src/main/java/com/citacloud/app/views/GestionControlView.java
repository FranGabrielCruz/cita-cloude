package com.citacloud.app.views;

import com.citacloud.app.models.Especialidad;
import com.citacloud.app.models.Medico;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.repositories.EspecialidadRepository;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.repositories.SucursalRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EmpresaService;
import com.citacloud.app.services.ManagementControlExcelService;
import com.citacloud.app.services.ManagementControlPdfService;
import com.citacloud.app.services.ManagementControlReportService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Route(value = "reportes/gestion-control", layout = MainLayout.class)
@PageTitle("Gestión y Control | CitaCloud")
@PermitAll
public class GestionControlView extends VerticalLayout implements BeforeEnterObserver {
    private final ManagementControlReportService reportes;
    private final ManagementControlPdfService pdf;
    private final ManagementControlExcelService excel;
    private final EmpresaService empresas;
    private final UUID empresa;
    private final SucursalRepository sucursales;
    private final MedicoRepository medicos;
    private final EspecialidadRepository especialidades;

    private final Select<String> periodo = new Select<>();
    private final DatePicker desde = new DatePicker("Fecha desde");
    private final DatePicker hasta = new DatePicker("Fecha hasta");
    private final ComboBox<Sucursal> sucursal = new ComboBox<>("Sucursal");
    private final ComboBox<Medico> medico = new ComboBox<>("Médico");
    private final ComboBox<Especialidad> especialidad = new ComboBox<>("Especialidad");
    private final Select<Integer> filasPorPagina = new Select<>();
    private final Div contenido = new Div();
    private int paginaProductividad = 0;

    public GestionControlView(ManagementControlReportService reportes, ManagementControlPdfService pdf,
                              ManagementControlExcelService excel, EmpresaService empresas,
                              SucursalRepository sucursales, MedicoRepository medicos,
                              EspecialidadRepository especialidades) {
        this.reportes = reportes;
        this.pdf = pdf;
        this.excel = excel;
        this.empresas = empresas;
        this.sucursales = sucursales;
        this.medicos = medicos;
        this.especialidades = especialidades;
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        empresa = usuario == null ? null : usuario.getEmpresaId();

        setWidthFull();
        setPadding(true);
        contenido.setWidthFull();
        contenido.getStyle().set("width", "100%");
        getStyle().set("padding-bottom", "5rem");
        configurar();
        add(encabezado(), filtros(), contenido);
        cargar();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!puede("reports.management.view")) event.forwardTo("");
    }

    private Component encabezado() {
        H2 titulo = new H2("Reporte de Gestión y Control");
        titulo.getStyle().set("margin", "0");
        Paragraph subtitulo = new Paragraph("Indicadores generales de rendimiento y operación.");
        VerticalLayout textos = new VerticalLayout(titulo, subtitulo);
        textos.setPadding(false);
        textos.setSpacing(false);
        String clinica = empresas.buscar(empresa).getNombre();

        StreamResource pdfRecurso = new StreamResource("gestion-control.pdf",
                () -> new ByteArrayInputStream(pdf.generar(empresa, filtrosActivos(), clinica)));
        Anchor exportarPdf = new Anchor(pdfRecurso, "");
        exportarPdf.getElement().setAttribute("download", true);
        Button botonPdf = new Button("Exportar PDF", VaadinIcon.DOWNLOAD.create());
        botonPdf.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportarPdf.add(botonPdf);

        StreamResource excelRecurso = new StreamResource("gestion-control.xlsx",
                () -> new ByteArrayInputStream(excel.generar(empresa, filtrosActivos(), clinica)));
        Anchor exportarExcel = new Anchor(excelRecurso, "");
        exportarExcel.getElement().setAttribute("download", true);
        Button botonExcel = new Button("Exportar Excel", VaadinIcon.TABLE.create());
        botonExcel.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        exportarExcel.add(botonExcel);

        HorizontalLayout acciones = new HorizontalLayout(exportarPdf, exportarExcel);
        acciones.setSpacing(false);
        acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout cabecera = new HorizontalLayout(textos, acciones);
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecera.setAlignItems(Alignment.CENTER);
        return cabecera;
    }

    private Component filtros() {
        HorizontalLayout fila = new HorizontalLayout(periodo, desde, hasta, sucursal, medico, especialidad);
        fila.setWidthFull();
        fila.setAlignItems(Alignment.END);
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> recargarDesdeInicio());
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> limpiarFiltros());
        limpiar.setTooltipText("Limpiar");
        limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        HorizontalLayout acciones = new HorizontalLayout(buscar, limpiar);
        acciones.setSpacing(false);
        acciones.setAlignItems(Alignment.CENTER);
        acciones.getStyle().set("gap", "0.35rem");
        fila.add(acciones);
        return fila;
    }

    private void configurar() {
        periodo.setLabel("Período");
        periodo.setItems("Hoy", "Ayer", "Esta semana", "Semana anterior", "Este mes", "Mes anterior",
                "Últimos 30 días", "Este año", "Personalizado");
        periodo.setValue("Últimos 30 días");
        desde.setValue(LocalDate.now().minusDays(29));
        hasta.setValue(LocalDate.now());
        periodo.addValueChangeListener(e -> { if (!"Personalizado".equals(e.getValue())) aplicarPeriodo(e.getValue()); });
        sucursal.setItems(sucursales.findByEmpresaIdAndActivaTrue(empresa).stream()
                .sorted(Comparator.comparing(Sucursal::getNombre)).toList());
        sucursal.setItemLabelGenerator(Sucursal::getNombre);
        medico.setItems(medicos.findByEmpresaId(empresa).stream().filter(m -> Boolean.TRUE.equals(m.getActivo())).toList());
        medico.setItemLabelGenerator(Medico::getNombreCompleto);
        especialidad.setItems(especialidades.findByEmpresaId(empresa).stream().filter(e -> Boolean.TRUE.equals(e.getActiva())).toList());
        especialidad.setItemLabelGenerator(Especialidad::getNombre);
        filasPorPagina.setLabel("Filas por página");
        filasPorPagina.setItems(5, 10, 15, 25, 50);
        filasPorPagina.setValue(10);
        filasPorPagina.addValueChangeListener(e -> recargarDesdeInicio());
        periodo.setWidth("180px"); desde.setWidth("150px"); hasta.setWidth("150px");
        sucursal.setWidth("220px"); medico.setWidth("220px"); especialidad.setWidth("220px");
    }

    private void aplicarPeriodo(String valor) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio;
        switch (valor) {
            case "Hoy" -> inicio = fin;
            case "Ayer" -> { inicio = fin.minusDays(1); fin = inicio; }
            case "Esta semana" -> inicio = fin.minusDays(fin.getDayOfWeek().getValue() - 1);
            case "Semana anterior" -> { fin = fin.minusDays(fin.getDayOfWeek().getValue()); inicio = fin.minusDays(6); }
            case "Este mes" -> inicio = fin.withDayOfMonth(1);
            case "Mes anterior" -> { fin = fin.withDayOfMonth(1).minusDays(1); inicio = fin.withDayOfMonth(1); }
            case "Este año" -> inicio = fin.withDayOfYear(1);
            default -> inicio = fin.minusDays(29);
        }
        desde.setValue(inicio);
        hasta.setValue(fin);
    }

    private void cargar() {
        contenido.removeAll();
        if (empresa == null) return;
        try {
            var reporte = reportes.generar(empresa, filtrosActivos());
            FlexLayout kpis = new FlexLayout();
            kpis.setWidthFull();
            kpis.getStyle().set("display", "grid").set("width", "100%")
                    .set("grid-template-columns", "repeat(auto-fit, minmax(210px, 1fr))").set("gap", "1rem");
            for (var indicador : reporte.resumen()) kpis.add(tarjetaKpi(indicador));

            Grid<ManagementControlReportService.Productividad> tabla = new Grid<>();
            int tamanoPagina = filasPorPagina.getValue() == null ? 10 : filasPorPagina.getValue();
            int totalRegistros = reporte.productividad().size();
            int totalPaginas = Math.max(1, (totalRegistros + tamanoPagina - 1) / tamanoPagina);
            paginaProductividad = Math.min(paginaProductividad, totalPaginas - 1);
            int inicioPagina = paginaProductividad * tamanoPagina;
            int finPagina = Math.min(inicioPagina + tamanoPagina, totalRegistros);
            tabla.setItems(reporte.productividad().subList(inicioPagina, finPagina));
            tabla.addColumn(ManagementControlReportService.Productividad::medico).setHeader("Médico");
            tabla.addColumn(ManagementControlReportService.Productividad::citas).setHeader("Citas");
            tabla.addColumn(ManagementControlReportService.Productividad::atendidas).setHeader("Atendidos");
            tabla.addColumn(ManagementControlReportService.Productividad::canceladas).setHeader("Canceladas");
            tabla.addColumn(ManagementControlReportService.Productividad::noShow).setHeader("No-show");
            tabla.addColumn(ManagementControlReportService.Productividad::promedioDia).setHeader("Prom./día");
            tabla.setAllRowsVisible(true);
            tabla.setWidthFull();

            Div paneles = new Div(
                    seccion("Actividad de citas", List.of("Programadas · Atendidas · Canceladas", "La actividad se agrupa según el período seleccionado.")),
                    seccion("Estado de citas", reporte.estados().stream().map(e -> "● " + e.estado() + "   " + e.cantidad() + "   " + e.porcentaje() + "%").toList()),
                    seccion("Tiempos de atención", List.of("ESPERA   " + reporte.tiempos().espera(), "CONSULTA   " + reporte.tiempos().consulta(), "TOTAL   " + reporte.tiempos().total(), "Sin datos suficientes")),
                    seccion("Pacientes", List.of("Atendidos   " + reporte.pacientesAtendidos(), "Nuevos   " + reporte.pacientesNuevos(), "Recurrentes   " + reporte.pacientesRecurrentes())),
                    seccion("Distribución por especialidad", reporte.especialidades().stream().map(e -> e.especialidad() + "  " + "█".repeat(Math.max(1, (int) Math.round(e.porcentaje() / 5))) + "  " + e.porcentaje() + "%").toList()),
                    seccion("Indicadores de control", reporte.indicadores().stream().map(i -> i.indicador() + "   " + i.resultado() + "   " + i.meta() + "   " + i.estado()).toList()));
            paneles.getStyle().set("display", "grid").set("width", "100%")
                    .set("grid-template-columns", "repeat(auto-fit, minmax(360px, 1fr))").set("gap", "1rem").set("margin-top", "1rem");
            contenido.add(new H3("Resumen ejecutivo"), kpis, paneles, new H3("Productividad médica"), tabla,
                    paginadorProductividad(totalRegistros, totalPaginas),
                    seccionCentrada("Alertas y oportunidades", reporte.alertas()));
        } catch (IllegalArgumentException ex) {
            contenido.add(new H3("No fue posible cargar el reporte"), new Paragraph(ex.getMessage()));
        }
    }

    private Component paginadorProductividad(int totalRegistros, int totalPaginas) {
        Button anterior = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            if (paginaProductividad > 0) { paginaProductividad--; cargar(); }
        });
        anterior.setTooltipText("Página anterior");
        anterior.setEnabled(paginaProductividad > 0);
        Button siguiente = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            if (paginaProductividad < totalPaginas - 1) { paginaProductividad++; cargar(); }
        });
        siguiente.setTooltipText("Página siguiente");
        siguiente.setEnabled(paginaProductividad < totalPaginas - 1);
        Span rango = new Span(totalRegistros == 0 ? "Sin registros" : String.format("Página %d de %d · %d registros",
                paginaProductividad + 1, totalPaginas, totalRegistros));
        HorizontalLayout navegacion = new HorizontalLayout(anterior, rango, siguiente);
        navegacion.setAlignItems(Alignment.CENTER);
        HorizontalLayout paginador = new HorizontalLayout(filasPorPagina, navegacion);
        paginador.setWidthFull();
        paginador.setAlignItems(Alignment.END);
        paginador.setJustifyContentMode(JustifyContentMode.BETWEEN);
        paginador.getStyle().set("padding", "0.25rem 0 1rem");
        return paginador;
    }

    private void recargarDesdeInicio() {
        paginaProductividad = 0;
        cargar();
    }

    private Component tarjetaKpi(ManagementControlReportService.Kpi indicador) {
        Span etiqueta = new Span(indicador.nombre().toUpperCase());
        etiqueta.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "600");
        H2 valor = new H2(indicador.valor());
        valor.getStyle().set("margin", "0.65rem 0");
        Span variacion = new Span(indicador.variacion() == null ? "— Sin comparación" : String.format(Locale.ROOT,
                "%s %.1f%% vs anterior", indicador.variacion() >= 0 ? "↑" : "↓", Math.abs(indicador.variacion())));
        Div tarjeta = new Div(etiqueta, valor, variacion);
        tarjeta.getStyle().set("padding", "1.2rem").set("width", "100%").set("box-sizing", "border-box")
                .set("background", "var(--lumo-base-color)").set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)").set("min-height", "130px");
        return tarjeta;
    }

    private Component seccion(String titulo, List<String> lineas) {
        VerticalLayout bloque = new VerticalLayout();
        bloque.setPadding(false); bloque.setSpacing(false);
        H3 encabezado = new H3(titulo);
        encabezado.getStyle().set("margin", "0 0 0.75rem 0");
        bloque.add(encabezado);
        lineas.forEach(texto -> {
            Div linea = new Div(new Span(texto));
            linea.getStyle().set("padding", "0.45rem 0").set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
            bloque.add(linea);
        });
        bloque.getStyle().set("padding", "1.2rem").set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)");
        return bloque;
    }

    private Component seccionCentrada(String titulo, List<String> lineas) {
        VerticalLayout bloque = new VerticalLayout();
        bloque.setPadding(false); bloque.setSpacing(false);
        bloque.setAlignItems(Alignment.CENTER);
        H3 encabezado = new H3(titulo);
        encabezado.getStyle().set("margin", "0 0 0.75rem 0").set("text-align", "center");
        bloque.add(encabezado);
        lineas.forEach(texto -> {
            Div linea = new Div(new Span(texto));
            linea.setWidthFull();
            linea.getStyle().set("padding", "0.45rem 0").set("text-align", "center")
                    .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
            bloque.add(linea);
        });
        bloque.getStyle().set("padding", "1.2rem").set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)");
        return bloque;
    }

    private void limpiarFiltros() {
        periodo.setValue("Últimos 30 días"); desde.setValue(LocalDate.now().minusDays(29)); hasta.setValue(LocalDate.now());
        sucursal.clear(); medico.clear(); especialidad.clear(); recargarDesdeInicio();
    }

    private ManagementControlReportService.Filtros filtrosActivos() {
        LocalDate fin = hasta.getValue() == null ? LocalDate.now() : hasta.getValue();
        LocalDate inicio = desde.getValue() == null ? fin.minusDays(29) : desde.getValue();
        if (inicio.isAfter(fin)) throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        return new ManagementControlReportService.Filtros(inicio, fin,
                sucursal.getValue() == null ? null : sucursal.getValue().getId(),
                medico.getValue() == null ? null : medico.getValue().getId(),
                especialidad.getValue() == null ? null : especialidad.getValue().getId());
    }

    private boolean puede(String permiso) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        return usuario != null && usuario.getAuthorities().stream().anyMatch(a -> permiso.equals(a.getAuthority())
                || "ROLE_ADMINISTRADOR".equals(a.getAuthority()) || "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }
}
