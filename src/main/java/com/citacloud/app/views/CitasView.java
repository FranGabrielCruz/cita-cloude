package com.citacloud.app.views;

import com.citacloud.app.models.Cita;
import com.citacloud.app.models.Consultorio;
import com.citacloud.app.models.Medico;
import com.citacloud.app.models.Paciente;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.CitaService;
import com.citacloud.app.services.ConsultorioService;
import com.citacloud.app.services.MedicoService;
import com.citacloud.app.services.PacienteService;
import com.citacloud.app.services.SucursalService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Route(value = "citas", layout = MainLayout.class)
@PageTitle("Agenda de Citas | CitaCloud")
@PermitAll
@CssImport("./styles/mobile-layouts.css")
public class CitasView extends VerticalLayout {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final SucursalService sucursalService;
    private final ConsultorioService consultorioService;
    private final UUID empresaId;
    private final Grid<Cita> grid = new Grid<>(Cita.class, false);
    private final PaginadorTabla<Cita> paginador = new PaginadorTabla<>(grid);
    private final ComboBox<Paciente> pacienteFiltro = new ComboBox<>("Paciente");
    private final ComboBox<Medico> medicoFiltro = new ComboBox<>("Medico");
    private final ComboBox<Sucursal> sucursalFiltro = new ComboBox<>("Sucursal");
    private final ComboBox<Consultorio> consultorioFiltro = new ComboBox<>("Consultorio");
    private final ComboBox<String> estadoFiltro = new ComboBox<>("Estado");

    public CitasView(CitaService citaService, PacienteService pacienteService, MedicoService medicoService,
                     SucursalService sucursalService, ConsultorioService consultorioService) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.sucursalService = sucursalService;
        this.consultorioService = consultorioService;
        TenantUserDetails user = AuthService.getAuthenticatedUser();
        this.empresaId = user == null ? null : user.getEmpresaId();

        // La tabla debe crecer con sus filas; en m\u00f3vil una altura fija la dejaba sin espacio visible.
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        configurarEncabezado();
        configurarFiltros();
        configurarTabla();
        add(crearEncabezado(), crearBarraFiltros(), grid, paginador);
        actualizarCitas();
    }

    private void configurarEncabezado() {
        estadoFiltro.setItems("Todos", "PENDIENTE", "CONFIRMADA", "EN_ESPERA", "EN_CONSULTA", "ATENDIDA", "CANCELADA");
        estadoFiltro.setItemLabelGenerator(this::etiquetaEstado);
        estadoFiltro.setValue("Todos");
        pacienteFiltro.setPlaceholder("Seleccione paciente");
        medicoFiltro.setPlaceholder("Seleccione médico");
        sucursalFiltro.setPlaceholder("Seleccione sucursal");
        consultorioFiltro.setPlaceholder("Seleccione consultorio");
        estadoFiltro.setPlaceholder("Seleccione estado");
        if (empresaId != null) {
            pacienteFiltro.setItems(pacienteService.listarActivos(empresaId));
            pacienteFiltro.setItemLabelGenerator(Paciente::getNombreCompleto);
            medicoFiltro.setItems(medicoService.listarActivos(empresaId));
            medicoFiltro.setItemLabelGenerator(Medico::getNombreCompleto);
            sucursalFiltro.setItems(sucursalService.listarActivas(empresaId));
            sucursalFiltro.setItemLabelGenerator(Sucursal::getNombre);
            consultorioFiltro.setItems(consultorioService.listarActivos(empresaId));
            consultorioFiltro.setItemLabelGenerator(Consultorio::getNombre);
        }
        sucursalFiltro.setClearButtonVisible(true);
        consultorioFiltro.setClearButtonVisible(true);
        pacienteFiltro.setClearButtonVisible(true);
        medicoFiltro.setClearButtonVisible(true);
    }

    private HorizontalLayout crearEncabezado() {
        H2 title = new H2("Agenda de Citas");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevaCita = new Button(VaadinIcon.PLUS.create(), e -> abrirFormulario(null));
        nuevaCita.setTooltipText("Nueva cita");
        nuevaCita.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevaCita.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizarCitas());
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> limpiarFiltros());
        limpiar.setTooltipText("Limpiar");
        limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        HorizontalLayout acciones = new HorizontalLayout(nuevaCita, buscar, limpiar);
        acciones.setSpacing(false);
        acciones.setAlignItems(FlexComponent.Alignment.CENTER);
        acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout header = new HorizontalLayout(title, acciones);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private void configurarFiltros() {
        sucursalFiltro.addValueChangeListener(event -> {
            Sucursal sucursal = event.getValue();
            if (sucursal == null || empresaId == null) {
                consultorioFiltro.setItems(empresaId == null ? List.of() : consultorioService.listarActivos(empresaId));
            } else {
                consultorioFiltro.setItems(consultorioService.listarActivos(empresaId).stream()
                        .filter(c -> sucursal.getId().equals(c.getSucursal().getId())).toList());
            }
            consultorioFiltro.clear();
        });
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout filtros = new HorizontalLayout(pacienteFiltro, medicoFiltro, sucursalFiltro, consultorioFiltro, estadoFiltro);
        filtros.addClassName("mobile-stacked-filters");
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.BASELINE);
        filtros.setFlexGrow(1, pacienteFiltro, medicoFiltro, sucursalFiltro, consultorioFiltro, estadoFiltro);
        pacienteFiltro.setMinWidth("150px");
        medicoFiltro.setMinWidth("150px");
        sucursalFiltro.setMinWidth("150px");
        consultorioFiltro.setMinWidth("150px");
        estadoFiltro.setMinWidth("150px");
        filtros.getStyle().set("background-color", "#ffffff").set("padding", "1rem").set("flex-wrap", "wrap")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        return filtros;
    }

    private void limpiarFiltros() {
        pacienteFiltro.clear();
        medicoFiltro.clear();
        estadoFiltro.setValue("Todos");
        sucursalFiltro.clear();
        consultorioFiltro.clear();
        actualizarCitas();
    }

    private void configurarTabla() {
        grid.addColumn(c -> c.getFecha() == null ? "-" : c.getFecha().toString()).setHeader("FECHA");
        grid.addColumn(c -> c.getHoraInicio() + " - " + c.getHoraFin()).setHeader("HORARIO");
        grid.addColumn(c -> c.getPaciente().getNombreCompleto()).setHeader("PACIENTE");
        grid.addColumn(c -> c.getMedico().getNombreCompleto()).setHeader("MEDICO");
        grid.addColumn(c -> c.getConsultorio() == null ? "-" : c.getConsultorio().getNombre()).setHeader("CONSULTORIO");
        grid.addColumn(Cita::getMotivo).setHeader("MOTIVO");
        grid.addComponentColumn(c -> {
            Span estado = new Span(etiquetaEstado(c.getEstado()));
            estado.addClassName("CANCELADA".equals(c.getEstado()) ? "badge-cancelada"
                    : "PENDIENTE".equals(c.getEstado()) || "EN_ESPERA".equals(c.getEstado()) ? "badge-pendiente" : "badge-confirmada");
            return estado;
        }).setHeader("ESTADO");
        grid.addComponentColumn(c -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirFormulario(c));
            editar.setTooltipText("Editar");
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES");
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.addClassName("citas-grid");
    }

    private void actualizarCitas() {
        if (empresaId == null) {
            paginador.setItems(List.of());
            return;
        }
        List<Cita> citas = citaService.listarPorEmpresa(empresaId).stream()
                .filter(c -> pacienteFiltro.getValue() == null || c.getPaciente().getId().equals(pacienteFiltro.getValue().getId()))
                .filter(c -> medicoFiltro.getValue() == null || c.getMedico().getId().equals(medicoFiltro.getValue().getId()))
                .filter(c -> sucursalFiltro.getValue() == null || c.getSucursal().getId().equals(sucursalFiltro.getValue().getId()))
                .filter(c -> consultorioFiltro.getValue() == null || (c.getConsultorio() != null
                        && c.getConsultorio().getId().equals(consultorioFiltro.getValue().getId())))
                .filter(c -> "Todos".equals(estadoFiltro.getValue()) || estadoFiltro.getValue().equals(c.getEstado()))
                .toList();
        paginador.setItems(citas);
    }

    private void abrirFormulario(Cita citaExistente) {
        if (empresaId == null) {
            Notification.show("No se pudo identificar la empresa de la sesión.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Dialog dialog = new Dialog();
        boolean esEdicion = citaExistente != null;
        dialog.setHeaderTitle(esEdicion ? "Editar cita" : "Registrar nueva cita");
        dialog.setWidth("760px");

        ComboBox<Paciente> paciente = selector("Paciente", pacienteService.listarActivos(empresaId), Paciente::getNombreCompleto);
        ComboBox<Medico> medico = selector("Medico", medicoService.listarActivos(empresaId), Medico::getNombreCompleto);
        ComboBox<Sucursal> sucursal = selector("Sucursal", sucursalService.listarActivas(empresaId), Sucursal::getNombre);
        ComboBox<Consultorio> consultorio = selector("Consultorio", consultorioService.listarActivos(empresaId), Consultorio::getNombre);
        DatePicker fecha = new DatePicker("Fecha");
        fecha.setValue(LocalDate.now());
        TimePicker horaInicio = new TimePicker("Hora de inicio");
        horaInicio.setValue(LocalTime.of(8, 0));
        TimePicker horaFin = new TimePicker("Hora de finalizacion");
        horaFin.setValue(LocalTime.of(8, 30));
        TextArea motivo = new TextArea("Motivo");
        motivo.setMaxLength(500);
        motivo.setWidthFull();
        ComboBox<String> estado = new ComboBox<>("Estado");
        estado.setItemLabelGenerator(this::etiquetaEstado);

        sucursal.addValueChangeListener(event -> {
            Sucursal seleccionada = event.getValue();
            consultorio.setItems(seleccionada == null ? List.of() : consultorioService.listarActivos(empresaId).stream()
                    .filter(c -> seleccionada.getId().equals(c.getSucursal().getId())).toList());
            consultorio.clear();
        });
        medico.addValueChangeListener(event -> sugerirHoraFin(medico, fecha, horaInicio, horaFin));
        fecha.addValueChangeListener(event -> sugerirHoraFin(medico, fecha, horaInicio, horaFin));
        horaInicio.addValueChangeListener(event -> sugerirHoraFin(medico, fecha, horaInicio, horaFin));

        if (esEdicion) {
            paciente.setValue(citaExistente.getPaciente());
            medico.setValue(citaExistente.getMedico());
            sucursal.setValue(citaExistente.getSucursal());
            consultorio.setValue(citaExistente.getConsultorio());
            fecha.setValue(citaExistente.getFecha());
            horaInicio.setValue(citaExistente.getHoraInicio());
            horaFin.setValue(citaExistente.getHoraFin());
            motivo.setValue(citaExistente.getMotivo() == null ? "" : citaExistente.getMotivo());
            estado.setItems(estadosEditables(citaExistente));
            estado.setValue(citaExistente.getEstado());
        } else {
            estado.setVisible(false);
        }

        // Dos columnas en computadora y una columna en pantallas peque\u00f1as.
        FormLayout formulario = new FormLayout(paciente, medico, sucursal, consultorio, fecha, horaInicio, horaFin, motivo, estado);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("620px", 2));
        formulario.setColspan(motivo, 2);
        formulario.setColspan(estado, 2);
        dialog.add(formulario);
        Button cerrar = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cerrar.setTooltipText("Cerrar");
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> {
            try {
                Cita cita = esEdicion ? citaExistente : new Cita();
                cita.setPaciente(paciente.getValue());
                cita.setMedico(medico.getValue());
                cita.setSucursal(sucursal.getValue());
                cita.setConsultorio(consultorio.getValue());
                cita.setFecha(fecha.getValue());
                cita.setHoraInicio(horaInicio.getValue());
                cita.setHoraFin(horaFin.getValue());
                cita.setMotivo(motivo.getValue());
                if (esEdicion) {
                    citaService.actualizar(empresaId, cita);
                    if (!citaExistente.getEstado().equals(estado.getValue())) {
                        cambiarEstadoDesdeEdicion(citaExistente, estado.getValue());
                    }
                } else {
                    citaService.registrar(empresaId, cita);
                }
                dialog.close();
                actualizarCitas();
                Notification.show(esEdicion ? "Cita actualizada correctamente." : "Cita registrada correctamente.",
                        3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        guardar.setTooltipText("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        if (esEdicion) {
            Button cancelarCita = new Button(VaadinIcon.CLOSE.create(),
                    e -> confirmarCancelacion(citaExistente, dialog));
            cancelarCita.setTooltipText("Cancelar cita");
            cancelarCita.setEnabled(!"CANCELADA".equals(citaExistente.getEstado()));
            cancelarCita.getStyle().set("background-color", "#dc2626").set("color", "#ffffff");
            dialog.getFooter().add(guardar);
            agregarAccionesEstado(dialog, citaExistente);
            dialog.getFooter().add(cancelarCita, cerrar);
        } else {
            dialog.getFooter().add(guardar, cerrar);
        }
        dialog.open();
    }

    private List<String> estadosEditables(Cita cita) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        boolean administrador = tieneRol(usuario, "ADMINISTRADOR");
        boolean medico = tieneRol(usuario, "MEDICO");
        boolean secretaria = tieneRol(usuario, "SECRETARIA") || tieneRol(usuario, "RECEPCIONISTA");
        if (administrador) return List.of(cita.getEstado(), "EN_ESPERA", "EN_CONSULTA", "ATENDIDA", "NO_ASISTIO").stream().distinct().toList();
        if (secretaria && "CONFIRMADA".equals(cita.getEstado())) return List.of("CONFIRMADA", "EN_ESPERA", "NO_ASISTIO");
        if (medico && ("CONFIRMADA".equals(cita.getEstado()) || "EN_ESPERA".equals(cita.getEstado()))) return List.of(cita.getEstado(), "EN_CONSULTA");
        if (medico && "EN_CONSULTA".equals(cita.getEstado())) return List.of("EN_CONSULTA", "ATENDIDA");
        return List.of(cita.getEstado());
    }

    private void cambiarEstadoDesdeEdicion(Cita cita, String nuevoEstado) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        citaService.cambiarEstadoClinico(empresaId, cita.getId(), usuario.getUsuarioId(),
                tieneRol(usuario, "ADMINISTRADOR"), tieneRol(usuario, "MEDICO"),
                tieneRol(usuario, "SECRETARIA") || tieneRol(usuario, "RECEPCIONISTA"), nuevoEstado);
    }

    private void sugerirHoraFin(ComboBox<Medico> medico, DatePicker fecha, TimePicker inicio, TimePicker fin) {
        if (medico.getValue() == null || fecha.getValue() == null || inicio.getValue() == null) return;
        citaService.obtenerHoraFinSugerida(empresaId, medico.getValue().getId(), fecha.getValue(), inicio.getValue())
                .ifPresent(fin::setValue);
    }

    private String etiquetaEstado(String estado) {
        if (estado == null) return "";
        return switch (estado) {
            case "EN_ESPERA" -> "En espera";
            case "EN_CONSULTA" -> "En consulta";
            case "CONFIRMADA" -> "Confirmada";
            case "PENDIENTE" -> "Pendiente";
            case "ATENDIDA" -> "Atendida";
            case "CANCELADA" -> "Cancelada";
            case "NO_ASISTIO" -> "No asistió";
            default -> estado;
        };
    }

    private void agregarAccionesEstado(Dialog dialog, Cita cita) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null || "CANCELADA".equals(cita.getEstado()) || "ATENDIDA".equals(cita.getEstado())) return;
        boolean administrador = tieneRol(usuario, "ADMINISTRADOR");
        boolean medico = tieneRol(usuario, "MEDICO");
        boolean secretaria = tieneRol(usuario, "SECRETARIA") || tieneRol(usuario, "RECEPCIONISTA");
        if ((administrador || secretaria) && "CONFIRMADA".equals(cita.getEstado())) {
            dialog.getFooter().add(botonEstado(cita, "EN_ESPERA", "Marcar en espera", VaadinIcon.CLOCK, "#d97706"));
        }
        if ((administrador || medico) && "EN_ESPERA".equals(cita.getEstado())) {
            dialog.getFooter().add(botonEstado(cita, "EN_CONSULTA", "Iniciar consulta", VaadinIcon.STETHOSCOPE, "#2563eb"));
        }
        if ((administrador || medico) && "EN_CONSULTA".equals(cita.getEstado())) {
            dialog.getFooter().add(botonEstado(cita, "ATENDIDA", "Marcar atendida", VaadinIcon.CHECK_CIRCLE, "#16a34a"));
        }
    }

    private Button botonEstado(Cita cita, String estado, String ayuda, VaadinIcon icono, String color) {
        Button boton = new Button(icono.create(), evento -> {
            TenantUserDetails usuario = AuthService.getAuthenticatedUser();
            try {
                citaService.cambiarEstadoClinico(empresaId, cita.getId(), usuario.getUsuarioId(),
                        tieneRol(usuario, "ADMINISTRADOR"), tieneRol(usuario, "MEDICO"),
                        tieneRol(usuario, "SECRETARIA") || tieneRol(usuario, "RECEPCIONISTA"), estado);
                UI.getCurrent().getPage().reload();
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        boton.setTooltipText(ayuda);
        boton.getStyle().set("background-color", color).set("color", "#ffffff");
        return boton;
    }

    private boolean tieneRol(TenantUserDetails usuario, String rol) {
        return usuario != null && usuario.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + rol).equalsIgnoreCase(authority.getAuthority()));
    }

    private void confirmarCancelacion(Cita cita, Dialog dialogEdicion) {
        Dialog confirmacion = new Dialog();
        confirmacion.setHeaderTitle("Cancelar cita");
        confirmacion.add(new Span("¿Estás seguro de que deseas cancelar esta cita?"));
        Button volver = new Button("No, volver", e -> confirmacion.close());
        Button confirmar = new Button("Sí, cancelar", e -> {
            try {
                citaService.cancelar(empresaId, cita.getId());
                confirmacion.close();
                dialogEdicion.close();
                actualizarCitas();
                Notification.show("Cita cancelada correctamente.", 3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        confirmacion.getFooter().add(volver, confirmar);
        confirmacion.open();
    }

    private <T> ComboBox<T> selector(String etiqueta, List<T> items, java.util.function.Function<T, String> etiquetaItem) {
        ComboBox<T> selector = new ComboBox<>(etiqueta);
        selector.setItems(items);
        selector.setItemLabelGenerator(etiquetaItem::apply);
        selector.setPlaceholder("Seleccione " + etiqueta.toLowerCase());
        selector.setRequiredIndicatorVisible(true);
        return selector;
    }
}
