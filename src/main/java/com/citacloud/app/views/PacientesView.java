package com.citacloud.app.views;

import com.citacloud.app.models.Paciente;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.PacienteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.UUID;

@Route(value = "pacientes", layout = MainLayout.class)
@PageTitle("Pacientes | CitaCloud")
@PermitAll
public class PacientesView extends VerticalLayout {

    private final PacienteService pacienteService;
    private final Grid<Paciente> grid = new Grid<>(Paciente.class, false);

    public PacientesView(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user == null ? null : user.getEmpresaId();
        TextField busqueda = new TextField();
        busqueda.setPlaceholder("Cédula o nombre...");
        busqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        busqueda.setWidth("300px");

        H2 titulo = new H2("Pacientes");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevo = new Button("Nuevo", VaadinIcon.PLUS.create(), e -> abrirModalPaciente(empresaId, null));
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a");
        Button buscar = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> actualizarBusqueda(empresaId, busqueda.getValue()));
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button("Limpiar", VaadinIcon.ERASER.create(), e -> {
            busqueda.clear();
            actualizarBusqueda(empresaId, "");
        });
        limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");

        HorizontalLayout acciones = new HorizontalLayout(nuevo, buscar, limpiar);
        acciones.setSpacing(false);
        acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout encabezado = new HorizontalLayout(titulo, acciones);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);

        ComboBox<String> estado = new ComboBox<>("Estado");
        estado.setItems("Todos", "Activo", "Inactivo");
        estado.setValue("Todos");
        ComboBox<String> seguro = new ComboBox<>("Seguro");
        seguro.setItems("Todos", "ARS Humano", "MAPFRE BHD", "Privado");
        seguro.setValue("Todos");
        HorizontalLayout filtros = new HorizontalLayout(busqueda, estado, seguro);
        filtros.setAlignItems(FlexComponent.Alignment.BASELINE);
        filtros.getStyle().set("background-color", "#ffffff").set("padding", "1rem")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");

        configurarTabla(empresaId);
        if (empresaId != null) {
            grid.setItems(pacienteService.listarPorEmpresa(empresaId));
        }
        add(encabezado, filtros, grid);
    }

    private void configurarTabla(UUID empresaId) {
        grid.addColumn(Paciente::getNombreCompleto).setHeader("PACIENTE");
        grid.addColumn(Paciente::getDocumento).setHeader("CÉDULA");
        grid.addColumn(Paciente::getTelefono).setHeader("TELÉFONO");
        grid.addComponentColumn(p -> new Span("ARS Humano")).setHeader("SEGURO");
        grid.addComponentColumn(p -> {
            Span estado = new Span(Boolean.TRUE.equals(p.getActivo()) ? "Activo" : "Inactivo");
            estado.addClassName(Boolean.TRUE.equals(p.getActivo()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(p -> {
            Button editar = new Button("Editar", VaadinIcon.EDIT.create(), e -> abrirModalPaciente(empresaId, p));
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            boolean activo = Boolean.TRUE.equals(p.getActivo());
            Button cambiarEstado = new Button(activo ? "Desactivar" : "Activar",
                    activo ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), e -> {
                try {
                    if (activo) {
                        pacienteService.desactivar(empresaId, p.getId());
                    } else {
                        pacienteService.activar(empresaId, p.getId());
                    }
                    actualizarBusqueda(empresaId, "");
                    Notification.show(activo ? "Paciente desactivado correctamente." : "Paciente activado correctamente.",
                            3000, Notification.Position.BOTTOM_START);
                } catch (IllegalArgumentException exception) {
                    Notification.show(exception.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            cambiarEstado.getStyle().set("color", activo ? "#b91c1c" : "#15803d");
            HorizontalLayout acciones = new HorizontalLayout(editar, cambiarEstado);
            acciones.setSpacing(false);
            acciones.getStyle().set("gap", "0.25rem");
            return acciones;
        }).setHeader("ACCIONES").setWidth("250px").setFlexGrow(0);
        grid.setWidthFull();
    }

    private void actualizarBusqueda(UUID empresaId, String termino) {
        if (empresaId != null) {
            grid.setItems(pacienteService.buscar(empresaId, termino));
        }
    }

    private void abrirModalPaciente(UUID empresaId, Paciente pacienteExistente) {
        if (empresaId == null) {
            return;
        }
        boolean esEdicion = pacienteExistente != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Paciente" : "Registrar Nuevo Paciente");
        TextField cedula = new TextField("Cédula");
        TextField nombre = new TextField("Nombre");
        TextField apellido = new TextField("Apellido");
        TextField telefono = new TextField("Teléfono");
        EmailField email = new EmailField("Email");
        cedula.setPlaceholder("000-0000000-0");
        telefono.setPlaceholder("(000) 000-0000");
        email.setPlaceholder("correo@ejemplo.com");
        cedula.setRequiredIndicatorVisible(true);
        nombre.setRequiredIndicatorVisible(true);
        apellido.setRequiredIndicatorVisible(true);
        telefono.setRequiredIndicatorVisible(true);
        email.setRequiredIndicatorVisible(true);
        cedula.addBlurListener(event -> cedula.setValue(formatearCedula(cedula.getValue())));
        telefono.addBlurListener(event -> telefono.setValue(formatearTelefono(telefono.getValue())));
        if (esEdicion) {
            cedula.setValue(pacienteExistente.getDocumento());
            nombre.setValue(pacienteExistente.getNombre());
            apellido.setValue(pacienteExistente.getApellido());
            telefono.setValue(pacienteExistente.getTelefono() == null ? "" : pacienteExistente.getTelefono());
            email.setValue(pacienteExistente.getEmail() == null ? "" : pacienteExistente.getEmail());
        }
        dialog.add(new FormLayout(cedula, nombre, apellido, telefono, email));

        Button cerrar = new Button("Cerrar", e -> dialog.close());
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        Button guardar = new Button("Guardar", VaadinIcon.DISC.create(), e -> {
            cedula.setValue(formatearCedula(cedula.getValue()));
            telefono.setValue(formatearTelefono(telefono.getValue()));
            if (!validarFormulario(cedula, nombre, apellido, telefono, email)) {
                return;
            }
            try {
                if (esEdicion) {
                    pacienteService.actualizar(empresaId, pacienteExistente.getId(), cedula.getValue(), nombre.getValue(),
                            apellido.getValue(), telefono.getValue(), email.getValue());
                } else {
                    Paciente paciente = new Paciente();
                    paciente.setEmpresaId(empresaId);
                    paciente.setDocumento(cedula.getValue());
                    paciente.setNombre(nombre.getValue());
                    paciente.setApellido(apellido.getValue());
                    paciente.setTelefono(telefono.getValue());
                    paciente.setEmail(email.getValue());
                    pacienteService.guardar(paciente);
                }
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
                return;
            }
            actualizarBusqueda(empresaId, "");
            dialog.close();
            Notification.show(esEdicion ? "Paciente actualizado con éxito" : "Paciente registrado con éxito");
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        dialog.getFooter().add(guardar, cerrar);
        dialog.open();
    }

    private boolean validarFormulario(TextField cedula, TextField nombre, TextField apellido,
                                      TextField telefono, EmailField email) {
        boolean valido = true;
        valido &= marcarInvalido(cedula, !cedula.getValue().matches("\\d{3}-\\d{7}-\\d"), "Formato: 000-0000000-0");
        valido &= marcarInvalido(nombre, nombre.isEmpty(), "El nombre es obligatorio");
        valido &= marcarInvalido(apellido, apellido.isEmpty(), "El apellido es obligatorio");
        valido &= marcarInvalido(telefono, !telefono.getValue().matches("\\(\\d{3}\\) \\d{3}-\\d{4}"), "Formato: (000) 000-0000");
        valido &= marcarInvalidoEmail(email, email.isEmpty() || !email.getValue().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"), "Ingresa un correo válido");
        return valido;
    }

    private boolean marcarInvalido(TextField campo, boolean invalido, String mensaje) {
        campo.setInvalid(invalido);
        campo.setErrorMessage(invalido ? mensaje : null);
        return !invalido;
    }

    private boolean marcarInvalidoEmail(EmailField campo, boolean invalido, String mensaje) {
        campo.setInvalid(invalido);
        campo.setErrorMessage(invalido ? mensaje : null);
        return !invalido;
    }

    private String formatearCedula(String valor) {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        return digitos.length() == 11 ? digitos.substring(0, 3) + "-" + digitos.substring(3, 10) + "-" + digitos.substring(10) : valor;
    }

    private String formatearTelefono(String valor) {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        return digitos.length() == 10 ? "(" + digitos.substring(0, 3) + ") " + digitos.substring(3, 6) + "-" + digitos.substring(6) : valor;
    }
}
