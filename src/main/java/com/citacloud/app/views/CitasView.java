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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
public class CitasView extends VerticalLayout {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final SucursalService sucursalService;
    private final ConsultorioService consultorioService;
    private final UUID empresaId;
    private final Grid<Cita> grid = new Grid<>(Cita.class, false);
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

        setSizeFull();
        setPadding(true);
        configurarEncabezado();
        configurarFiltros();
        configurarTabla();
        add(crearEncabezado(), crearBarraFiltros(), grid);
        actualizarCitas();
    }

    private void configurarEncabezado() {
        estadoFiltro.setItems("Todos", "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA");
        estadoFiltro.setValue("Todos");
        pacienteFiltro.setPlaceholder("Seleccione paciente");
        medicoFiltro.setPlaceholder("Seleccione médico");
        sucursalFiltro.setPlaceholder("Seleccione sucursal");
        consultorioFiltro.setPlaceholder("Seleccione consultorio");
        estadoFiltro.setPlaceholder("Seleccione estado");
        if (empresaId != null) {
            pacienteFiltro.setItems(pacienteService.listarPorEmpresa(empresaId));
            pacienteFiltro.setItemLabelGenerator(Paciente::getNombreCompleto);
            medicoFiltro.setItems(medicoService.listarActivos(empresaId));
            medicoFiltro.setItemLabelGenerator(Medico::getNombreCompleto);
            sucursalFiltro.setItems(sucursalService.listarPorEmpresa(empresaId));
            sucursalFiltro.setItemLabelGenerator(Sucursal::getNombre);
            consultorioFiltro.setItems(consultorioService.listarPorEmpresa(empresaId));
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
        Button nuevaCita = new Button("Nueva", VaadinIcon.PLUS.create(), e -> abrirFormulario(null));
        nuevaCita.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevaCita.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> actualizarCitas());
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button("Limpiar", VaadinIcon.ERASER.create(), e -> limpiarFiltros());
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
                consultorioFiltro.setItems(empresaId == null ? List.of() : consultorioService.listarPorEmpresa(empresaId));
            } else {
                consultorioFiltro.setItems(consultorioService.listarPorEmpresa(empresaId).stream()
                        .filter(c -> sucursal.getId().equals(c.getSucursal().getId())).toList());
            }
            consultorioFiltro.clear();
        });
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout filtros = new HorizontalLayout(pacienteFiltro, medicoFiltro, sucursalFiltro, consultorioFiltro, estadoFiltro);
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
            Span estado = new Span(c.getEstado());
            estado.addClassName("CANCELADA".equals(c.getEstado()) ? "badge-cancelada"
                    : "PENDIENTE".equals(c.getEstado()) ? "badge-pendiente" : "badge-confirmada");
            return estado;
        }).setHeader("ESTADO");
        grid.addComponentColumn(c -> {
            Button editar = new Button("Editar", VaadinIcon.EDIT.create(), e -> abrirFormulario(c));
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES");
        grid.setWidthFull();
    }

    private void actualizarCitas() {
        if (empresaId == null) {
            grid.setItems(List.of());
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
        grid.setItems(citas);
    }

    private void abrirFormulario(Cita citaExistente) {
        if (empresaId == null) {
            Notification.show("No se pudo identificar la empresa de la sesión.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Dialog dialog = new Dialog();
        boolean esEdicion = citaExistente != null;
        dialog.setHeaderTitle(esEdicion ? "Editar cita" : "Registrar nueva cita");
        dialog.setWidth("680px");

        ComboBox<Paciente> paciente = selector("Paciente", pacienteService.listarPorEmpresa(empresaId), Paciente::getNombreCompleto);
        ComboBox<Medico> medico = selector("Medico", medicoService.listarActivos(empresaId), Medico::getNombreCompleto);
        ComboBox<Sucursal> sucursal = selector("Sucursal", sucursalService.listarPorEmpresa(empresaId), Sucursal::getNombre);
        ComboBox<Consultorio> consultorio = selector("Consultorio", consultorioService.listarPorEmpresa(empresaId), Consultorio::getNombre);
        DatePicker fecha = new DatePicker("Fecha");
        fecha.setValue(LocalDate.now());
        TimePicker horaInicio = new TimePicker("Hora de inicio");
        horaInicio.setValue(LocalTime.of(8, 0));
        TimePicker horaFin = new TimePicker("Hora de finalizacion");
        horaFin.setValue(LocalTime.of(8, 30));
        TextArea motivo = new TextArea("Motivo");
        motivo.setMaxLength(500);
        motivo.setWidthFull();

        sucursal.addValueChangeListener(event -> {
            Sucursal seleccionada = event.getValue();
            consultorio.setItems(seleccionada == null ? List.of() : consultorioService.listarPorEmpresa(empresaId).stream()
                    .filter(c -> seleccionada.getId().equals(c.getSucursal().getId())).toList());
            consultorio.clear();
        });

        if (esEdicion) {
            paciente.setValue(citaExistente.getPaciente());
            medico.setValue(citaExistente.getMedico());
            sucursal.setValue(citaExistente.getSucursal());
            consultorio.setValue(citaExistente.getConsultorio());
            fecha.setValue(citaExistente.getFecha());
            horaInicio.setValue(citaExistente.getHoraInicio());
            horaFin.setValue(citaExistente.getHoraFin());
            motivo.setValue(citaExistente.getMotivo() == null ? "" : citaExistente.getMotivo());
        }

        FormLayout formulario = new FormLayout(paciente, medico, sucursal, consultorio, fecha, horaInicio, horaFin, motivo);
        formulario.setColspan(motivo, 2);
        dialog.add(formulario);
        Button cerrar = new Button("Cerrar", e -> dialog.close());
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        Button guardar = new Button(esEdicion ? "Guardar" : "Registrar cita", VaadinIcon.DISC.create(), e -> {
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
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        if (esEdicion) {
            Button cancelarCita = new Button("Cancelar cita", VaadinIcon.CLOSE.create(),
                    e -> confirmarCancelacion(citaExistente, dialog));
            cancelarCita.setEnabled(!"CANCELADA".equals(citaExistente.getEstado()));
            cancelarCita.getStyle().set("background-color", "#dc2626").set("color", "#ffffff");
            dialog.getFooter().add(guardar, cancelarCita, cerrar);
        } else {
            dialog.getFooter().add(guardar, cerrar);
        }
        dialog.open();
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
