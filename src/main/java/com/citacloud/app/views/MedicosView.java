package com.citacloud.app.views;

import com.citacloud.app.models.Especialidad;
import com.citacloud.app.models.Medico;
import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EspecialidadService;
import com.citacloud.app.services.MedicoService;
import com.citacloud.app.services.SucursalService;
import com.citacloud.app.repositories.RolRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.UUID;

@Route(value = "medicos", layout = MainLayout.class)
@PageTitle("M\u00e9dicos | CitaCloud")
@PermitAll
public class MedicosView extends VerticalLayout {

    private final MedicoService medicoService;
    private final EspecialidadService especialidadService;
    private final SucursalService sucursalService;
    private final RolRepository rolRepository;
    private final UUID empresaId;
    private final Grid<Medico> grid = new Grid<>(Medico.class, false);
    private final TextField codigoFiltro = new TextField("C\u00f3digo");
    private final TextField nombreFiltro = new TextField("Nombre");
    private final ComboBox<Especialidad> especialidadFiltro = new ComboBox<>("Especialidad");

    public MedicosView(MedicoService medicoService, EspecialidadService especialidadService,
                       SucursalService sucursalService, RolRepository rolRepository) {
        this.medicoService = medicoService;
        this.especialidadService = especialidadService;
        this.sucursalService = sucursalService;
        this.rolRepository = rolRepository;
        TenantUserDetails user = AuthService.getAuthenticatedUser();
        this.empresaId = user == null ? null : user.getEmpresaId();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        configurarFiltros();
        configurarTabla();
        add(crearEncabezado(), crearBarraFiltros(), grid);
        actualizarMedicos();
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("M\u00e9dicos");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> abrirFormulario(null));
        nuevo.setTooltipText("Nuevo m\u00e9dico");
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizarMedicos());
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> limpiarFiltros());
        limpiar.setTooltipText("Limpiar");
        limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        HorizontalLayout acciones = new HorizontalLayout(nuevo, buscar, limpiar);
        acciones.setSpacing(false);
        acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout encabezado = new HorizontalLayout(titulo, acciones);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        return encabezado;
    }

    private void configurarFiltros() {
        codigoFiltro.setPlaceholder("Buscar por c\u00f3digo");
        nombreFiltro.setPlaceholder("Buscar por nombre");
        especialidadFiltro.setPlaceholder("Seleccione especialidad");
        especialidadFiltro.setClearButtonVisible(true);
        especialidadFiltro.setItemLabelGenerator(Especialidad::getNombre);
        if (empresaId != null) {
            especialidadFiltro.setItems(especialidadService.listarActivas(empresaId));
        }
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout filtros = new HorizontalLayout(codigoFiltro, nombreFiltro, especialidadFiltro);
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.BASELINE);
        filtros.setFlexGrow(1, codigoFiltro, nombreFiltro, especialidadFiltro);
        filtros.getStyle().set("background-color", "#ffffff").set("padding", "1rem")
                .set("flex-wrap", "wrap").set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        return filtros;
    }

    private void configurarTabla() {
        grid.addColumn(Medico::getCodigo).setHeader("C\u00d3DIGO");
        grid.addColumn(Medico::getNombreCompleto).setHeader("NOMBRE");
        grid.addColumn(Medico::getExequatur).setHeader("EXEQU\u00c1TUR");
        grid.addColumn(Medico::getEspecialidadesTexto).setHeader("ESPECIALIDADES");
        grid.addColumn(medico -> medico.getSucursal() == null ? "-" : medico.getSucursal().getNombre()).setHeader("SUCURSAL");
        grid.addColumn(Medico::getTelefono).setHeader("TEL\u00c9FONO");
        grid.addColumn(Medico::getEmail).setHeader("EMAIL");
        grid.addComponentColumn(medico -> {
            Span estado = new Span(Boolean.TRUE.equals(medico.getActivo()) ? "Activo" : "Inactivo");
            estado.addClassName(Boolean.TRUE.equals(medico.getActivo()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO");
        grid.addComponentColumn(medico -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirFormulario(medico));
            editar.setTooltipText("Editar m\u00e9dico");
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES").setWidth("120px").setFlexGrow(0);
        grid.setWidthFull();
    }

    private void actualizarMedicos() {
        if (empresaId == null) {
            grid.setItems(List.of());
            return;
        }
        Especialidad especialidad = especialidadFiltro.getValue();
        grid.setItems(medicoService.buscar(empresaId, codigoFiltro.getValue(), nombreFiltro.getValue(),
                especialidad == null ? null : especialidad.getId()));
    }

    private void limpiarFiltros() {
        codigoFiltro.clear();
        nombreFiltro.clear();
        especialidadFiltro.clear();
        actualizarMedicos();
    }

    private void abrirFormulario(Medico medicoExistente) {
        if (empresaId == null) {
            Notification.show("No se pudo identificar la empresa de la sesi\u00f3n.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Dialog dialog = new Dialog();
        boolean esEdicion = medicoExistente != null;
        dialog.setHeaderTitle(esEdicion ? "Editar M\u00e9dico" : "Nuevo M\u00e9dico");
        dialog.setWidth("760px");

        TextField nombre = new TextField("Nombre");
        TextField apellido = new TextField("Apellido");
        TextField cedula = new TextField("C\u00e9dula");
        TextField telefono = new TextField("Tel\u00e9fono");
        EmailField correo = new EmailField("Correo");
        TextField exequatur = new TextField("Exequ\u00e1tur");
        MultiSelectComboBox<Especialidad> especialidades = new MultiSelectComboBox<>("Especialidades");
        ComboBox<Sucursal> sucursal = new ComboBox<>("Sucursal");
        Checkbox crearUsuario = new Checkbox("Crear usuario para este m\u00e9dico", true);
        TextField usuario = new TextField("Usuario");
        PasswordField contrasena = new PasswordField("Contrase\u00f1a");
        PasswordField confirmarContrasena = new PasswordField("Confirmar contrase\u00f1a");
        ComboBox<Rol> rol = new ComboBox<>("Rol");
        Checkbox usuarioActivo = new Checkbox("Usuario activo", true);

        cedula.setPlaceholder("000-0000000-0");
        telefono.setPlaceholder("(000) 000-0000");
        correo.setPlaceholder("correo@ejemplo.com");
        usuario.setValue("");
        contrasena.setValue("");
        confirmarContrasena.setValue("");
        usuario.getElement().setAttribute("autocomplete", "off");
        contrasena.getElement().setAttribute("autocomplete", "new-password");
        confirmarContrasena.getElement().setAttribute("autocomplete", "new-password");
        especialidades.setPlaceholder("Seleccione especialidades");
        sucursal.setPlaceholder("Seleccione sucursal");
        especialidades.setItems(especialidadService.listarActivas(empresaId));
        especialidades.setItemLabelGenerator(Especialidad::getNombre);
        sucursal.setItems(sucursalService.listarActivas(empresaId));
        sucursal.setItemLabelGenerator(Sucursal::getNombre);
        rol.setItems(rolRepository.findByEmpresaId(empresaId));
        rol.setItemLabelGenerator(Rol::getNombre);
        rol.setValue(rolRepository.findByEmpresaIdAndNombre(empresaId, "MEDICO").orElse(null));
        cedula.addBlurListener(event -> cedula.setValue(formatearCedula(cedula.getValue())));
        telefono.addBlurListener(event -> telefono.setValue(formatearTelefono(telefono.getValue())));
        nombre.setRequiredIndicatorVisible(true);
        apellido.setRequiredIndicatorVisible(true);
        cedula.setRequiredIndicatorVisible(true);
        exequatur.setRequiredIndicatorVisible(true);
        especialidades.setRequiredIndicatorVisible(true);
        sucursal.setRequiredIndicatorVisible(true);

        FormLayout personales = new FormLayout(nombre, apellido, cedula, telefono, correo);
        FormLayout profesionales = new FormLayout(exequatur, especialidades, sucursal);
        profesionales.setColspan(especialidades, 2);
        FormLayout acceso = new FormLayout(usuario, contrasena, confirmarContrasena, rol, usuarioActivo);
        VerticalLayout accesoContenido = new VerticalLayout(acceso);
        accesoContenido.setPadding(false);
        accesoContenido.setSpacing(false);
        crearUsuario.addValueChangeListener(event -> accesoContenido.setVisible(event.getValue()));
        if (esEdicion) {
            crearUsuario.setValue(false);
            crearUsuario.setVisible(false);
            accesoContenido.setVisible(false);
            nombre.setValue(medicoExistente.getNombre());
            apellido.setValue(medicoExistente.getApellido());
            cedula.setValue(medicoExistente.getCedula() == null ? "" : medicoExistente.getCedula());
            telefono.setValue(medicoExistente.getTelefono() == null ? "" : medicoExistente.getTelefono());
            correo.setValue(medicoExistente.getEmail() == null ? "" : medicoExistente.getEmail());
            exequatur.setValue(medicoExistente.getExequatur() == null ? "" : medicoExistente.getExequatur());
            especialidades.setValue(medicoExistente.getEspecialidades());
            sucursal.setValue(medicoExistente.getSucursal());
        }
        VerticalLayout formulario = new VerticalLayout(
                new H4("DATOS PERSONALES"), personales,
                new H4("DATOS PROFESIONALES"), profesionales,
                new H4("ACCESO AL SISTEMA"), crearUsuario, accesoContenido);
        formulario.setPadding(false);
        formulario.setSpacing(false);
        formulario.getChildren().filter(componente -> componente instanceof H4)
                .forEach(componente -> componente.getStyle().set("margin", "1rem 0 0.5rem"));
        dialog.add(formulario);

        Button cancelar = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cancelar.setTooltipText("Cancelar");
        cancelar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> {
            try {
                Medico medico = new Medico();
                medico.setEmpresaId(empresaId);
                medico.setNombre(nombre.getValue());
                medico.setApellido(apellido.getValue());
                medico.setCedula(formatearCedula(cedula.getValue()));
                medico.setTelefono(formatearTelefono(telefono.getValue()));
                medico.setEmail(correo.getValue());
                medico.setExequatur(exequatur.getValue());
                medico.setEspecialidades(especialidades.getValue());
                medico.setSucursal(sucursal.getValue());
                if (esEdicion) {
                    medicoService.actualizar(empresaId, medicoExistente.getId(), medico);
                } else {
                    medicoService.registrar(empresaId, medico, crearUsuario.getValue(), usuario.getValue(),
                            contrasena.getValue(), confirmarContrasena.getValue(),
                            rol.getValue() == null ? "MEDICO" : rol.getValue().getNombre(), usuarioActivo.getValue());
                }
                dialog.close();
                actualizarMedicos();
                Notification.show(esEdicion ? "M\u00e9dico actualizado correctamente." : "M\u00e9dico registrado correctamente.",
                        3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        guardar.setTooltipText("Guardar m\u00e9dico");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        dialog.getFooter().add(guardar);
        if (esEdicion) {
            Button cambiarEstado = new Button(medicoExistente.getActivo() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), e -> {
                try {
                    medicoService.cambiarEstado(empresaId, medicoExistente.getId(), !medicoExistente.getActivo());
                    dialog.close();
                    actualizarMedicos();
                    Notification.show(medicoExistente.getActivo() ? "M\u00e9dico desactivado." : "M\u00e9dico activado.",
                            3000, Notification.Position.BOTTOM_START);
                } catch (IllegalArgumentException exception) {
                    Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            cambiarEstado.setTooltipText(medicoExistente.getActivo() ? "Desactivar m\u00e9dico" : "Activar m\u00e9dico");
            cambiarEstado.getStyle().set("background-color", medicoExistente.getActivo() ? "#dc2626" : "#16a34a")
                    .set("color", "#ffffff");
            dialog.getFooter().add(cambiarEstado);
        }
        dialog.getFooter().add(cancelar);
        dialog.open();
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
