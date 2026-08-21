package com.citacloud.app.views;

import com.citacloud.app.models.Aseguradora;
import com.citacloud.app.models.Paciente;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.PacienteService;
import com.citacloud.app.services.AseguradoraService;
import com.citacloud.app.services.SeguroPacienteService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.UUID;

@Route(value = "pacientes", layout = MainLayout.class)
@PageTitle("Pacientes | CitaCloud")
@PermitAll
@CssImport("./styles/mobile-layouts.css")
public class PacientesView extends VerticalLayout {

    private final PacienteService pacienteService;
    private final AseguradoraService aseguradoraService;
    private final SeguroPacienteService seguroPacienteService;
    private final Grid<Paciente> grid = new Grid<>(Paciente.class, false);
    private final PaginadorTabla<Paciente> paginador = new PaginadorTabla<>(grid);

    public PacientesView(PacienteService pacienteService, AseguradoraService aseguradoraService,
                         SeguroPacienteService seguroPacienteService) {
        this.pacienteService = pacienteService;
        this.aseguradoraService = aseguradoraService;
        this.seguroPacienteService = seguroPacienteService;
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

        Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> abrirModalPaciente(empresaId, null));
        nuevo.setTooltipText("Nuevo paciente");
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizarBusqueda(empresaId, busqueda.getValue()));
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> {
            busqueda.clear();
            actualizarBusqueda(empresaId, "");
        });
        limpiar.setTooltipText("Limpiar");
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
        ComboBox<Aseguradora> seguro = new ComboBox<>("Seguro");
        seguro.setItemLabelGenerator(Aseguradora::getNombre);
        seguro.setPlaceholder("Seleccione seguro");
        if (empresaId != null) {
            seguro.setItems(aseguradoraService.listarActivas(empresaId));
        }
        HorizontalLayout filtros = new HorizontalLayout(busqueda, estado, seguro);
        filtros.addClassName("mobile-stacked-filters");
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.BASELINE);
        filtros.getStyle().set("background-color", "#ffffff").set("padding", "1rem")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");

        configurarTabla(empresaId);
        if (empresaId != null) {
            paginador.setItems(pacienteService.listarPorEmpresa(empresaId));
        }
        add(encabezado, filtros, grid, paginador);
    }

    private void configurarTabla(UUID empresaId) {
        grid.addColumn(Paciente::getNombreCompleto).setHeader("PACIENTE");
        grid.addColumn(Paciente::getDocumento).setHeader("CÉDULA");
        grid.addColumn(Paciente::getTelefono).setHeader("TELÉFONO");
        grid.addComponentColumn(p -> new Span(seguroPacienteService.nombreSeguroActivo(empresaId, p.getId())))
                .setHeader("SEGURO");
        grid.addComponentColumn(p -> {
            Span estado = new Span(Boolean.TRUE.equals(p.getActivo()) ? "Activo" : "Inactivo");
            estado.addClassName(Boolean.TRUE.equals(p.getActivo()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(p -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirModalPaciente(empresaId, p));
            editar.setTooltipText("Editar");
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES").setWidth("120px").setFlexGrow(0);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
    }

    private void actualizarBusqueda(UUID empresaId, String termino) {
        if (empresaId != null) {
            paginador.setItems(pacienteService.buscar(empresaId, termino));
        }
    }

    private void abrirModalPaciente(UUID empresaId, Paciente pacienteExistente) {
        if (empresaId == null) {
            return;
        }
        boolean esEdicion = pacienteExistente != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Paciente" : "Registrar Nuevo Paciente");
        ComboBox<String> tipoDocumento = new ComboBox<>("Tipo de documento", "CEDULA", "PASAPORTE", "OTRO", "SIN_DOCUMENTO");
        TextField cedula = new TextField("Número de documento");
        TextField nombre = new TextField("Nombre");
        TextField apellido = new TextField("Apellido");
        DatePicker nacimiento = new DatePicker("Fecha de nacimiento");
        ComboBox<String> sexo = new ComboBox<>("Sexo", "MASCULINO", "FEMENINO", "OTRO");
        TextField telefono = new TextField("Teléfono");
        EmailField email = new EmailField("Email");
        TextArea direccion = new TextArea("Dirección");
        TextField nacionalidad = new TextField("Nacionalidad");
        ComboBox<String> provincia = new ComboBox<>("Provincia", "Distrito Nacional", "Santo Domingo", "Santiago", "La Altagracia", "Otra");
        TextField municipio = new TextField("Municipio");
        TextField telefonoAlternativo = new TextField("Teléfono alternativo");
        TextField contactoEmergencia = new TextField("Contacto de emergencia");
        TextField telefonoEmergencia = new TextField("Teléfono de emergencia");
        ComboBox<String> parentesco = new ComboBox<>("Parentesco", "Madre", "Padre", "Esposo/a", "Hijo/a", "Hermano/a", "Otro");
        ComboBox<Aseguradora> seguro = new ComboBox<>("Seguro");
        TextField numeroPoliza = new TextField("N\u00famero de p\u00f3liza");
        cedula.setPlaceholder("000-0000000-0");
        telefono.setPlaceholder("(000) 000-0000");
        email.setPlaceholder("correo@ejemplo.com");
        seguro.setPlaceholder("Seleccione seguro");
        seguro.setItemLabelGenerator(Aseguradora::getNombre);
        seguro.setClearButtonVisible(true);
        seguro.setItems(aseguradoraService.listarActivas(empresaId));
        numeroPoliza.setPlaceholder("Ingrese el n\u00famero de p\u00f3liza");
        tipoDocumento.setValue("CEDULA");
        nombre.setRequiredIndicatorVisible(true);
        apellido.setRequiredIndicatorVisible(true);
        telefono.setRequiredIndicatorVisible(true);
        nacimiento.setRequiredIndicatorVisible(true); sexo.setRequiredIndicatorVisible(true);
        cedula.addBlurListener(event -> cedula.setValue(formatearCedula(cedula.getValue())));
        telefono.addBlurListener(event -> telefono.setValue(formatearTelefono(telefono.getValue())));
        if (esEdicion) {
            cedula.setValue(pacienteExistente.getDocumento() == null ? "" : pacienteExistente.getDocumento());
            tipoDocumento.setValue(pacienteExistente.getTipoDocumento() == null ? "CEDULA" : pacienteExistente.getTipoDocumento()); nacimiento.setValue(pacienteExistente.getFechaNacimiento()); if(pacienteExistente.getGenero()!=null) sexo.setValue(pacienteExistente.getGenero()); direccion.setValue(pacienteExistente.getDireccion()==null?"":pacienteExistente.getDireccion()); nacionalidad.setValue(pacienteExistente.getNacionalidad()==null?"":pacienteExistente.getNacionalidad()); if(pacienteExistente.getProvincia()!=null) provincia.setValue(pacienteExistente.getProvincia()); municipio.setValue(pacienteExistente.getMunicipio()==null?"":pacienteExistente.getMunicipio()); telefonoAlternativo.setValue(pacienteExistente.getTelefonoAlternativo()==null?"":pacienteExistente.getTelefonoAlternativo()); contactoEmergencia.setValue(pacienteExistente.getContactoEmergencia()==null?"":pacienteExistente.getContactoEmergencia()); telefonoEmergencia.setValue(pacienteExistente.getTelefonoEmergencia()==null?"":pacienteExistente.getTelefonoEmergencia()); if(pacienteExistente.getParentescoEmergencia()!=null) parentesco.setValue(pacienteExistente.getParentescoEmergencia());
            nombre.setValue(pacienteExistente.getNombre());
            apellido.setValue(pacienteExistente.getApellido());
            telefono.setValue(pacienteExistente.getTelefono() == null ? "" : pacienteExistente.getTelefono());
            email.setValue(pacienteExistente.getEmail() == null ? "" : pacienteExistente.getEmail());
            seguroPacienteService.aseguradoraDelSeguroActivo(empresaId, pacienteExistente.getId())
                    .ifPresent(seguro::setValue);
            seguroPacienteService.obtenerActivo(empresaId, pacienteExistente.getId())
                    .ifPresent(seguroPaciente -> numeroPoliza.setValue(seguroPaciente.getNumeroPoliza()));
        }
        tipoDocumento.addValueChangeListener(e -> { boolean sin="SIN_DOCUMENTO".equals(e.getValue()); cedula.setVisible(!sin); if(sin)cedula.clear(); });
        FormLayout formulario=new FormLayout(); formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0",1),new FormLayout.ResponsiveStep("720px",2),new FormLayout.ResponsiveStep("1080px",3)); H2 personales=new H2("Datos personales"),contacto=new H2("Contacto"),emergencia=new H2("Contacto de emergencia"),medico=new H2("Seguro médico"); formulario.add(personales,nombre,apellido,nacimiento,sexo,tipoDocumento,cedula,contacto,telefono,telefonoAlternativo,email,direccion,nacionalidad,provincia,municipio,emergencia,contactoEmergencia,parentesco,telefonoEmergencia,medico,seguro,numeroPoliza); formulario.setColspan(personales,3);formulario.setColspan(contacto,3);formulario.setColspan(emergencia,3);formulario.setColspan(medico,3);formulario.setColspan(direccion,3); dialog.setWidth("min(1100px,96vw)");dialog.add(formulario);

        Button cerrar = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cerrar.setTooltipText("Cerrar");
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> {
            cedula.setValue(formatearCedula(cedula.getValue()));
            telefono.setValue(formatearTelefono(telefono.getValue()));
            if (!"SIN_DOCUMENTO".equals(tipoDocumento.getValue()) && !validarFormulario(cedula, nombre, apellido, telefono, email)) {
                return;
            }
            if (seguro.getValue() != null && numeroPoliza.isEmpty()) {
                numeroPoliza.setInvalid(true);
                numeroPoliza.setErrorMessage("Ingresa el nÃºmero de pÃ³liza.");
                return;
            }
            numeroPoliza.setInvalid(false);
            try {
                Paciente pacienteGuardado;
                if (esEdicion) {
                    pacienteGuardado = pacienteService.actualizar(empresaId, pacienteExistente.getId(), cedula.getValue(), nombre.getValue(),
                            apellido.getValue(), telefono.getValue(), email.getValue());
                    pacienteGuardado.setTipoDocumento(tipoDocumento.getValue()); pacienteGuardado.setFechaNacimiento(nacimiento.getValue()); pacienteGuardado.setGenero(sexo.getValue()); pacienteGuardado.setDireccion(direccion.getValue()); pacienteGuardado.setNacionalidad(nacionalidad.getValue()); pacienteGuardado.setProvincia(provincia.getValue()); pacienteGuardado.setMunicipio(municipio.getValue()); pacienteGuardado.setTelefonoAlternativo(telefonoAlternativo.getValue()); pacienteGuardado.setContactoEmergencia(contactoEmergencia.getValue()); pacienteGuardado.setTelefonoEmergencia(telefonoEmergencia.getValue()); pacienteGuardado.setParentescoEmergencia(parentesco.getValue());
                    pacienteGuardado = pacienteService.actualizarPerfil(empresaId, pacienteGuardado);
                } else {
                    Paciente paciente = new Paciente();
                    paciente.setEmpresaId(empresaId);
                    paciente.setDocumento("SIN_DOCUMENTO".equals(tipoDocumento.getValue())?null:cedula.getValue()); paciente.setTipoDocumento(tipoDocumento.getValue()); paciente.setFechaNacimiento(nacimiento.getValue()); paciente.setGenero(sexo.getValue()); paciente.setDireccion(direccion.getValue()); paciente.setNacionalidad(nacionalidad.getValue()); paciente.setProvincia(provincia.getValue()); paciente.setMunicipio(municipio.getValue()); paciente.setTelefonoAlternativo(telefonoAlternativo.getValue()); paciente.setContactoEmergencia(contactoEmergencia.getValue()); paciente.setTelefonoEmergencia(telefonoEmergencia.getValue()); paciente.setParentescoEmergencia(parentesco.getValue());
                    paciente.setNombre(nombre.getValue());
                    paciente.setApellido(apellido.getValue());
                    paciente.setTelefono(telefono.getValue());
                    paciente.setEmail(email.getValue());
                    pacienteGuardado = pacienteService.guardar(paciente);
                }
                seguroPacienteService.actualizarSeguro(empresaId, pacienteGuardado.getId(), seguro.getValue(), numeroPoliza.getValue());
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
                return;
            }
            actualizarBusqueda(empresaId, "");
            dialog.close();
            Notification.show(esEdicion ? "Paciente actualizado con éxito" : "Paciente registrado con éxito");
        });
        guardar.setTooltipText("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        dialog.getFooter().add(guardar);
        if (esEdicion) {
            boolean activo = Boolean.TRUE.equals(pacienteExistente.getActivo());
            Button cambiarEstado = new Button(activo ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), e -> {
                try {
                    if (activo) {
                        pacienteService.desactivar(empresaId, pacienteExistente.getId());
                    } else {
                        pacienteService.activar(empresaId, pacienteExistente.getId());
                    }
                    dialog.close();
                    actualizarBusqueda(empresaId, "");
                    Notification.show(activo ? "Paciente desactivado correctamente." : "Paciente activado correctamente.",
                            3000, Notification.Position.BOTTOM_START);
                } catch (IllegalArgumentException exception) {
                    Notification.show(exception.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            cambiarEstado.setTooltipText(activo ? "Desactivar paciente" : "Activar paciente");
            cambiarEstado.getStyle().set("background-color", activo ? "#dc2626" : "#16a34a").set("color", "#ffffff");
            dialog.getFooter().add(cambiarEstado);
        }
        dialog.getFooter().add(cerrar);
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
