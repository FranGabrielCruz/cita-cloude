package com.citacloud.app.views;

import com.citacloud.app.models.Documento;
import com.citacloud.app.models.Paciente;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.DocumentoService;
import com.citacloud.app.services.PacienteService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Route(value = "documentos", layout = MainLayout.class)
@PageTitle("Documentos | CitaCloud")
@PermitAll
public class DocumentosView extends VerticalLayout {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uu");
    private final DocumentoService documentos;
    private final PacienteService pacientes;
    private final UUID empresaId;
    private final UUID usuarioId;
    private final ComboBox<Paciente> paciente = new ComboBox<>("Paciente");
    private final TextField busqueda = new TextField();
    private final ComboBox<String> tipoFiltro = new ComboBox<>();
    private final ComboBox<String> estadoFiltro = new ComboBox<>();
    private final DatePicker fechaFiltro = new DatePicker();
    private final VerticalLayout contextoPaciente = new VerticalLayout();
    private final HorizontalLayout resumen = new HorizontalLayout();
    private final Grid<Documento> tabla = new Grid<>(Documento.class, false);
    private final PaginadorTabla<Documento> paginador = new PaginadorTabla<>(tabla);

    public DocumentosView(DocumentoService documentos, PacienteService pacientes) {
        this.documentos = documentos;
        this.pacientes = pacientes;
        TenantUserDetails sesion = AuthService.getAuthenticatedUser();
        empresaId = sesion == null ? null : sesion.getEmpresaId();
        usuarioId = sesion == null ? null : sesion.getUsuarioId();
        setSizeFull();
        setPadding(true);
        getStyle().set("padding-bottom", "12rem").set("box-sizing", "border-box");

        configurarSelectorPaciente();
        configurarTabla();
        add(encabezado(), new Paragraph("Consulta y administra los documentos clínicos del paciente."),
                contextoPaciente, resumen, filtros(), new H3("Documentos recientes"), tabla, paginador);
        actualizar();
    }

    private Component encabezado() {
        H2 titulo = new H2("Documentos");
        Button subir = new Button("Subir documento", VaadinIcon.UPLOAD.create(), e -> dialogoCarga());
        subir.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout encabezado = new HorizontalLayout(titulo, subir);
        encabezado.setWidthFull();
        encabezado.setAlignItems(Alignment.CENTER);
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return encabezado;
    }

    private void configurarSelectorPaciente() {
        paciente.setItems(empresaId == null ? List.of() : pacientes.listarPorEmpresa(empresaId));
        paciente.setItemLabelGenerator(Paciente::getNombreCompleto);
        paciente.setPlaceholder("Seleccione un paciente");
        paciente.setWidth("22rem");
        paciente.addValueChangeListener(evento -> actualizar());
        if (!paciente.getListDataView().getItems().toList().isEmpty()) {
            paciente.setValue(paciente.getListDataView().getItems().findFirst().orElse(null));
        }
    }

    private void configurarTabla() {
        tabla.addColumn(documento -> icono(documento) + " " + documento.getNombre()).setHeader("NOMBRE").setAutoWidth(true);
        tabla.addColumn(documento -> etiquetaTipo(documento)).setHeader("TIPO").setAutoWidth(true);
        tabla.addColumn(documento -> documento.getFecha() == null ? "—" : FECHA.format(documento.getFecha().toLocalDate()))
                .setHeader("FECHA").setAutoWidth(true);
        tabla.addColumn(documento -> origen(documento)).setHeader("ORIGEN").setAutoWidth(true);
        tabla.addComponentColumn(this::acciones).setHeader("ACCIONES").setAutoWidth(true).setFlexGrow(0);
        tabla.setWidthFull();
        tabla.setAllRowsVisible(true);
    }

    private Component filtros() {
        busqueda.setPlaceholder("Buscar documento...");
        busqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        busqueda.setClearButtonVisible(true);
        busqueda.addValueChangeListener(evento -> actualizar());
        busqueda.setWidthFull();
        tipoFiltro.setLabel("Tipo");
        tipoFiltro.setItems("Todos", "PDF", "Imagen");
        tipoFiltro.setValue("Todos");
        tipoFiltro.addValueChangeListener(evento -> actualizar());
        estadoFiltro.setLabel("Estado");
        estadoFiltro.setItems("Activos", "Archivados", "Todos");
        estadoFiltro.setValue("Activos");
        estadoFiltro.addValueChangeListener(evento -> actualizar());
        fechaFiltro.setLabel("Fecha");
        fechaFiltro.addValueChangeListener(evento -> actualizar());
        HorizontalLayout filtros = new HorizontalLayout(paciente, busqueda, tipoFiltro, estadoFiltro, fechaFiltro);
        filtros.setWidthFull();
        filtros.setAlignItems(Alignment.END);
        filtros.setFlexGrow(1, busqueda);
        return filtros;
    }

    private void actualizar() {
        List<Documento> todos = empresaId == null ? List.of() : documentos.listar(empresaId).stream()
                .filter(documento -> paciente.getValue() == null || (documento.getPaciente() != null
                        && paciente.getValue().getId().equals(documento.getPaciente().getId())))
                .toList();
        pintarContexto();
        pintarResumen(todos);
        String termino = busqueda.getValue() == null ? "" : busqueda.getValue().trim().toLowerCase(Locale.ROOT);
        List<Documento> filtrados = todos.stream()
                .filter(documento -> termino.isBlank() || documento.getNombre().toLowerCase(Locale.ROOT).contains(termino)
                        || etiquetaTipo(documento).toLowerCase(Locale.ROOT).contains(termino))
                .filter(documento -> "Todos".equals(tipoFiltro.getValue()) || coincideTipo(documento, tipoFiltro.getValue()))
                .filter(documento -> coincideEstado(documento))
                .filter(documento -> fechaFiltro.getValue() == null || (documento.getFecha() != null
                        && fechaFiltro.getValue().equals(documento.getFecha().toLocalDate())))
                .toList();
        paginador.setItems(filtrados);
    }

    private void pintarContexto() {
        contextoPaciente.removeAll();
        contextoPaciente.setPadding(true);
        contextoPaciente.setSpacing(false);
        contextoPaciente.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "12px");
        if (paciente.getValue() == null) {
            contextoPaciente.add(new Span("👤 Selecciona un paciente para consultar su repositorio documental."));
            return;
        }
        Paciente actual = paciente.getValue();
        contextoPaciente.add(new Span("👤 " + actual.getNombreCompleto().toUpperCase()),
                new Span("Expediente: " + valor(actual.getNumeroExpediente()) + " · " + edad(actual)));
    }

    private void pintarResumen(List<Documento> documentosPaciente) {
        resumen.removeAll();
        long resultados = documentosPaciente.stream().filter(documento -> categoria(documento).equals("RESULTADO")).count();
        long informes = documentosPaciente.stream().filter(documento -> categoria(documento).equals("INFORME")).count();
        long otros = documentosPaciente.size() - resultados - informes;
        resumen.add(tarjeta("📄 Todos", documentosPaciente.size()), tarjeta("🧪 Resultados", resultados),
                tarjeta("📝 Informes", informes), tarjeta("📎 Otros", otros));
        resumen.setWidthFull();
        resumen.getStyle().set("flex-wrap", "wrap");
    }

    private Component tarjeta(String titulo, long cantidad) {
        VerticalLayout tarjeta = new VerticalLayout(new Span(titulo), new H2(String.valueOf(cantidad)));
        tarjeta.setPadding(true);
        tarjeta.setSpacing(false);
        tarjeta.getStyle().set("min-width", "180px").set("flex", "1")
                .set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "12px");
        return tarjeta;
    }

    private Component acciones(Documento documento) {
        Button ver = new Button(VaadinIcon.EYE.create(), evento -> ver(documento));
        ver.setTooltipText("Ver documento");
        Button descargar = new Button(VaadinIcon.DOWNLOAD.create(), evento -> descargar(documento));
        descargar.setTooltipText("Descargar");
        ver.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        descargar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        HorizontalLayout acciones = new HorizontalLayout(ver, descargar);
        if ("ACTIVO".equals(documento.getEstado()) && "MANUAL".equals(documento.getOrigen())) {
            Button mas = new Button(VaadinIcon.ELLIPSIS_DOTS_V.create());
            mas.setTooltipText("Más opciones");
            ContextMenu menu = new ContextMenu(mas);
            menu.setOpenOnClick(true);
            menu.addItem("✏ Editar información", evento -> dialogoEditar(documento));
            menu.addItem("ℹ Ver detalles", evento -> dialogoDetalles(documento));
            menu.addItem("🗃 Archivar documento", evento -> dialogoArchivar(documento));
            acciones.add(mas);
        } else {
            Button detalles = new Button(VaadinIcon.INFO_CIRCLE.create(), evento -> dialogoDetalles(documento));
            detalles.setTooltipText("Ver detalles");
            detalles.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            acciones.add(detalles);
        }
        return acciones;
    }

    private void ver(Documento documento) {
        getUI().ifPresent(ui -> ui.getPage().open("documentos/" + documento.getId() + "/ver", "_blank"));
    }

    private void descargar(Documento documento) {
        getUI().ifPresent(ui -> ui.getPage().open("documentos/" + documento.getId(), "_blank"));
    }

    private void dialogoCarga() {
        if (empresaId == null) return;
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Subir documento");
        ComboBox<Paciente> pacienteCarga = new ComboBox<>("Paciente *", paciente.getListDataView().getItems().toList());
        pacienteCarga.setItemLabelGenerator(Paciente::getNombreCompleto);
        pacienteCarga.setValue(paciente.getValue());
        TextField nombre = new TextField("Título / nombre *");
        TextArea descripcion = new TextArea("Descripción");
        MemoryBuffer buffer = new MemoryBuffer();
        Upload carga = new Upload(buffer);
        carga.setAcceptedFileTypes("application/pdf", "image/png", "image/jpeg");
        carga.setMaxFiles(1);
        carga.setMaxFileSize(10 * 1024 * 1024);
        carga.setDropLabel(new Span("Arrastra el archivo aquí o selecciónalo. PDF, JPG, JPEG o PNG · Máximo 10 MB."));
        FormLayout formulario = new FormLayout(nombre, pacienteCarga, descripcion, carga);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("720px", 2));
        formulario.setColspan(descripcion, 2);
        formulario.setColspan(carga, 2);
        dialogo.setWidth("min(760px, 96vw)");
        Button guardar = new Button(VaadinIcon.DISC.create(), evento -> guardar(dialogo, pacienteCarga, nombre, descripcion, buffer));
        guardar.setTooltipText("Guardar documento");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button cancelar = new Button(VaadinIcon.CLOSE.create(), evento -> dialogo.close());
        cancelar.setTooltipText("Cancelar");
        cancelar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        dialogo.add(formulario);
        dialogo.getFooter().add(guardar, cancelar);
        dialogo.open();
    }

    private void guardar(Dialog dialogo, ComboBox<Paciente> pacienteCarga, TextField nombre, TextArea descripcion,
                         MemoryBuffer buffer) {
        try (InputStream contenido = buffer.getInputStream()) {
            String archivo = buffer.getFileName();
            if (pacienteCarga.getValue() == null || archivo == null || archivo.isBlank()) {
                throw new IllegalArgumentException("Selecciona el paciente y el archivo del documento.");
            }
            documentos.subir(empresaId, usuarioId, pacienteCarga.getValue().getId(), nombre.getValue(), descripcion.getValue(),
                    archivo, mime(archivo), contenido, 1);
            dialogo.close();
            actualizar();
            Notification.show("Documento guardado correctamente.");
        } catch (Exception error) {
            Notification.show(error.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void dialogoEditar(Documento documento) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Editar documento");
        TextField titulo = new TextField("Título *", documento.getNombre());
        DatePicker fecha = new DatePicker("Fecha del documento");
        if (documento.getFecha() != null) fecha.setValue(documento.getFecha().toLocalDate());
        TextArea descripcion = new TextArea("Descripción", documento.getDescripcion() == null ? "" : documento.getDescripcion());
        Span archivo = new Span("📄 " + documento.getNombreArchivo() + " · " + etiquetaTipo(documento));
        Span aviso = new Span("El archivo original y el paciente no se modificarán.");
        FormLayout formulario = new FormLayout(archivo, aviso, titulo, fecha, descripcion);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("680px", 2));
        formulario.setColspan(archivo, 2);
        formulario.setColspan(aviso, 2);
        formulario.setColspan(descripcion, 2);
        Button guardar = new Button(VaadinIcon.DISC.create(), evento -> {
            try {
                documentos.editarMetadatos(empresaId, usuarioId, documento.getId(), titulo.getValue(), descripcion.getValue(),
                        fecha.getValue() == null ? null : fecha.getValue().atStartOfDay());
                dialogo.close();
                actualizar();
                Notification.show("Documento actualizado correctamente.");
            } catch (IllegalArgumentException error) {
                Notification.show(error.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        guardar.setTooltipText("Guardar cambios");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button cancelar = botonCerrar(dialogo, "Cancelar");
        dialogo.setWidth("min(760px, 96vw)");
        dialogo.add(formulario);
        dialogo.getFooter().add(guardar, cancelar);
        dialogo.open();
    }

    private void dialogoArchivar(Documento documento) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Archivar documento");
        ComboBox<String> motivo = new ComboBox<>("Motivo de archivo *");
        motivo.setItems("Documento cargado por error", "Documento duplicado", "Archivo incorrecto", "Documento reemplazado", "Otro");
        TextArea detalles = new TextArea("Detalles");
        Span mensaje = new Span("📄 " + documento.getNombre() + "\nEl documento se ocultará de los activos, pero conservará su trazabilidad.");
        Button archivar = new Button(VaadinIcon.ARCHIVE.create(), evento -> {
            try {
                String razon = motivo.getValue();
                if ("Otro".equals(razon)) razon = detalles.getValue();
                else if (razon != null && !detalles.getValue().isBlank()) razon += ": " + detalles.getValue().trim();
                documentos.archivar(empresaId, usuarioId, documento.getId(), razon);
                dialogo.close();
                actualizar();
                Notification.show("Documento archivado correctamente.");
            } catch (IllegalArgumentException error) {
                Notification.show(error.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        archivar.setTooltipText("Archivar documento");
        archivar.getStyle().set("background-color", "#b45309").set("color", "#ffffff");
        Button cancelar = botonCerrar(dialogo, "Cancelar");
        dialogo.add(new VerticalLayout(mensaje, motivo, detalles));
        dialogo.getFooter().add(archivar, cancelar);
        dialogo.open();
    }

    private void dialogoDetalles(Documento documento) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Detalles del documento");
        VerticalLayout detalles = new VerticalLayout(
                detalle("Título", documento.getNombre()),
                detalle("Archivo", documento.getNombreArchivo()),
                detalle("Tipo", etiquetaTipo(documento)),
                detalle("Paciente", documento.getPaciente() == null ? "—" : documento.getPaciente().getNombreCompleto()),
                detalle("Expediente", documento.getPaciente() == null ? "—" : valor(documento.getPaciente().getNumeroExpediente())),
                detalle("Origen", origen(documento)),
                detalle("Fecha del documento", documento.getFecha() == null ? "—" : FECHA.format(documento.getFecha().toLocalDate())),
                detalle("Estado", documento.getEstado()));
        if ("ARCHIVADO".equals(documento.getEstado())) {
            detalles.add(detalle("Motivo de archivo", valor(documento.getMotivoArchivo())),
                    detalle("Archivado", documento.getArchivadoEn() == null ? "—" : documento.getArchivadoEn().format(DateTimeFormatter.ofPattern("dd/MM/uu · h:mm a"))));
        }
        Button cerrar = botonCerrar(dialogo, "Cerrar");
        dialogo.setWidth("min(650px, 96vw)");
        dialogo.add(detalles);
        dialogo.getFooter().add(cerrar);
        dialogo.open();
    }

    private Component detalle(String etiqueta, String contenido) {
        return new Div(new Span(etiqueta + ": "), new Span(valor(contenido)));
    }

    private Button botonCerrar(Dialog dialogo, String etiqueta) {
        Button cerrar = new Button(VaadinIcon.CLOSE.create(), evento -> dialogo.close());
        cerrar.setTooltipText(etiqueta);
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        return cerrar;
    }

    private boolean coincideTipo(Documento documento, String tipo) {
        return "PDF".equals(tipo) ? "application/pdf".equals(documento.getTipo())
                : "Imagen".equals(tipo) && documento.getTipo() != null && documento.getTipo().startsWith("image/");
    }

    private boolean coincideEstado(Documento documento) {
        return "Todos".equals(estadoFiltro.getValue()) || ("Activos".equals(estadoFiltro.getValue())
                && "ACTIVO".equals(documento.getEstado())) || ("Archivados".equals(estadoFiltro.getValue())
                && "ARCHIVADO".equals(documento.getEstado()));
    }

    private String categoria(Documento documento) {
        String texto = (documento.getNombre() + " " + valor(documento.getDescripcion())).toLowerCase(Locale.ROOT);
        if (texto.contains("resultado") || texto.contains("hemograma") || texto.contains("laboratorio")) return "RESULTADO";
        return texto.contains("informe") ? "INFORME" : "OTRO";
    }

    private String etiquetaTipo(Documento documento) {
        return "application/pdf".equals(documento.getTipo()) ? "Documento PDF"
                : documento.getTipo() != null && documento.getTipo().startsWith("image/") ? "Imagen clínica" : "Otro";
    }

    private String icono(Documento documento) {
        return documento.getTipo() != null && documento.getTipo().startsWith("image/") ? "🖼" : "📄";
    }

    private String origen(Documento documento) {
        return "MANUAL".equals(documento.getOrigen()) ? "Manual" : documento.getOrigen();
    }

    private String edad(Paciente paciente) {
        return paciente.getFechaNacimiento() == null ? "Edad no registrada"
                : Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears() + " años";
    }

    private String mime(String archivo) {
        String nombre = archivo.toLowerCase(Locale.ROOT);
        return nombre.endsWith(".pdf") ? "application/pdf" : nombre.endsWith(".png") ? "image/png" : "image/jpeg";
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
