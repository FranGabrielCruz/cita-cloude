package com.citacloud.app.views;

import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.UsuarioService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.UUID;

@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | CitaCloud")
@PermitAll
@CssImport("./styles/mobile-layouts.css")
public class UsuariosView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final UUID empresaId;
    private final Grid<Usuario> grid = new Grid<>(Usuario.class, false);
    private final PaginadorTabla<Usuario> paginador = new PaginadorTabla<>(grid);
    private final TextField usuarioFiltro = new TextField("Usuario");
    private final TextField nombreFiltro = new TextField("Nombre");
    private final ComboBox<Rol> rolFiltro = new ComboBox<>("Rol");
    private final ComboBox<String> estadoFiltro = new ComboBox<>("Estado");

    public UsuariosView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        TenantUserDetails sesion = AuthService.getAuthenticatedUser();
        empresaId = sesion == null ? null : sesion.getEmpresaId();
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        configurarFiltros();
        configurarTabla();
        add(crearEncabezado(), crearFiltros(), grid, paginador);
        actualizarUsuarios();
    }

    private void configurarFiltros() {
        usuarioFiltro.setPlaceholder("Buscar por usuario"); usuarioFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        nombreFiltro.setPlaceholder("Buscar por nombre"); nombreFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        rolFiltro.setPlaceholder("Seleccione rol"); rolFiltro.setItemLabelGenerator(Rol::getNombre);
        estadoFiltro.setItems("Activo", "Inactivo"); estadoFiltro.setPlaceholder("Seleccione estado");
        if (empresaId != null) rolFiltro.setItems(usuarioService.listarRoles(empresaId));
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Usuarios");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevo = new Button(VaadinIcon.PLUS.create(), event -> abrirFormulario(null));
        nuevo.setTooltipText("Nuevo usuario"); nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), event -> actualizarUsuarios());
        buscar.setTooltipText("Buscar"); buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), event -> limpiarFiltros());
        limpiar.setTooltipText("Limpiar"); limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        HorizontalLayout acciones = new HorizontalLayout(nuevo, buscar, limpiar);
        acciones.setSpacing(false); acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout encabezado = new HorizontalLayout(titulo, acciones);
        encabezado.setWidthFull(); encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER); return encabezado;
    }

    private HorizontalLayout crearFiltros() {
        HorizontalLayout filtros = new HorizontalLayout(usuarioFiltro, nombreFiltro, rolFiltro, estadoFiltro);
        filtros.addClassName("mobile-stacked-filters"); filtros.setWidthFull();
        filtros.setFlexGrow(1, usuarioFiltro, nombreFiltro, rolFiltro, estadoFiltro);
        filtros.getStyle().set("flex-wrap", "wrap").set("background", "#ffffff").set("padding", "1rem")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        return filtros;
    }

    private void configurarTabla() {
        grid.addColumn(Usuario::getUsuario).setHeader("USUARIO");
        grid.addColumn(Usuario::getNombreCompleto).setHeader("NOMBRE");
        grid.addColumn(Usuario::getEmail).setHeader("EMAIL");
        grid.addColumn(usuario -> usuario.getRoles().stream().map(Rol::getNombre).sorted().reduce((a, b) -> a + ", " + b).orElse("-"))
                .setHeader("ROL");
        grid.addComponentColumn(usuario -> { Span estado = new Span(Boolean.TRUE.equals(usuario.getActivo()) ? "Activo" : "Inactivo"); estado.addClassName(Boolean.TRUE.equals(usuario.getActivo()) ? "badge-activo" : "badge-inactivo"); return estado; }).setHeader("ESTADO").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(usuario -> { Button editar = new Button(VaadinIcon.EDIT.create(), event -> abrirFormulario(usuario)); editar.setTooltipText("Editar"); editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY); return editar; }).setHeader("ACCIONES").setWidth("115px").setFlexGrow(0);
        grid.setWidthFull();
    }

    private void actualizarUsuarios() {
        if (empresaId == null) { paginador.setItems(List.of()); return; }
        Boolean activo = "Activo".equals(estadoFiltro.getValue()) ? Boolean.TRUE : "Inactivo".equals(estadoFiltro.getValue()) ? Boolean.FALSE : null;
        paginador.setItems(usuarioService.buscar(empresaId, usuarioFiltro.getValue(), nombreFiltro.getValue(), rolFiltro.getValue() == null ? null : rolFiltro.getValue().getId(), activo));
    }

    private void limpiarFiltros() { usuarioFiltro.clear(); nombreFiltro.clear(); rolFiltro.clear(); estadoFiltro.clear(); actualizarUsuarios(); }

    private void abrirFormulario(Usuario existente) {
        if (empresaId == null) { Notification.show("No se pudo identificar la empresa de la sesi\u00f3n.", 3000, Notification.Position.MIDDLE); return; }
        boolean esEdicion = existente != null;
        Dialog dialog = new Dialog(); dialog.setHeaderTitle(esEdicion ? "Editar Usuario" : "Nuevo Usuario"); dialog.setWidth("650px");
        TextField usuario = new TextField("Usuario"); usuario.setRequiredIndicatorVisible(true); usuario.setMaxLength(50);
        TextField nombre = new TextField("Nombre"); nombre.setRequiredIndicatorVisible(true); nombre.setMaxLength(100);
        TextField apellido = new TextField("Apellido"); apellido.setRequiredIndicatorVisible(true); apellido.setMaxLength(100);
        EmailField email = new EmailField("Correo"); email.setMaxLength(100);
        TextField telefono = new TextField("Tel\u00e9fono"); telefono.setPlaceholder("(000) 000-0000"); telefono.setMaxLength(30);
        ComboBox<Rol> rol = new ComboBox<>("Rol"); rol.setRequiredIndicatorVisible(true); rol.setItems(usuarioService.listarRoles(empresaId)); rol.setItemLabelGenerator(Rol::getNombre);
        PasswordField contrasena = new PasswordField("Contrase\u00f1a"); contrasena.setRequiredIndicatorVisible(!esEdicion);
        PasswordField confirmacion = new PasswordField("Confirmar contrase\u00f1a"); confirmacion.setRequiredIndicatorVisible(!esEdicion);
        contrasena.getElement().setAttribute("autocomplete", "new-password");
        confirmacion.getElement().setAttribute("autocomplete", "new-password");
        contrasena.clear();
        confirmacion.clear();
        if (esEdicion) {
            usuario.setValue(existente.getUsuario()); nombre.setValue(existente.getNombre()); apellido.setValue(existente.getApellido());
            email.setValue(existente.getEmail() == null ? "" : existente.getEmail()); telefono.setValue(existente.getTelefono() == null ? "" : existente.getTelefono());
            if (!existente.getRoles().isEmpty()) rol.setValue(existente.getRoles().iterator().next());
            contrasena.setHelperText("Déjala vacía para conservar la actual.");
        }
        FormLayout formulario = new FormLayout(usuario, nombre, apellido, email, telefono, rol, contrasena, confirmacion);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2)); dialog.add(formulario);
        Button guardar = new Button(VaadinIcon.DISC.create(), event -> {
            try {
                if (esEdicion) usuarioService.actualizar(empresaId, existente.getId(), usuario.getValue(), nombre.getValue(), apellido.getValue(), email.getValue(), telefono.getValue(), contrasena.getValue(), confirmacion.getValue(), rol.getValue());
                else usuarioService.crear(empresaId, usuario.getValue(), nombre.getValue(), apellido.getValue(), email.getValue(), telefono.getValue(), contrasena.getValue(), confirmacion.getValue(), rol.getValue());
                dialog.close(); actualizarUsuarios(); Notification.show(esEdicion ? "Usuario actualizado." : "Usuario creado.", 3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) { Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE); }
        });
        guardar.setTooltipText("Guardar"); guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button cerrar = new Button(VaadinIcon.CLOSE.create(), event -> dialog.close()); cerrar.setTooltipText("Cerrar"); cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        dialog.getFooter().add(guardar);
        if (esEdicion) {
            boolean activo = Boolean.TRUE.equals(existente.getActivo());
            Button estado = new Button(activo ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), event -> {
                try { usuarioService.cambiarEstado(empresaId, existente.getId(), !activo); dialog.close(); actualizarUsuarios(); Notification.show(activo ? "Usuario desactivado." : "Usuario activado.", 3000, Notification.Position.BOTTOM_START); }
                catch (IllegalArgumentException exception) { Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE); }
            });
            estado.setTooltipText(activo ? "Desactivar usuario" : "Activar usuario"); estado.getStyle().set("background-color", activo ? "#dc2626" : "#16a34a").set("color", "#ffffff"); dialog.getFooter().add(estado);
        }
        dialog.getFooter().add(cerrar); dialog.open();
    }
}
