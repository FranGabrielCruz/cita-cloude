package com.citacloud.app.views;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.services.EmpresaAdministracionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "empresas", layout = MainLayout.class)
@PageTitle("Empresas | CitaCloud")
@PermitAll
public class EmpresasView extends VerticalLayout implements BeforeEnterObserver {
    private final EmpresaAdministracionService empresaService;
    private final Grid<Empresa> empresas = new Grid<>(Empresa.class, false);
    private final TextField codigoFiltro = new TextField("C\u00f3digo");
    private final TextField nombreFiltro = new TextField("Nombre");
    private final ComboBox<String> estadoFiltro = new ComboBox<>("Estado");

    public EmpresasView(EmpresaAdministracionService empresaService) {
        this.empresaService = empresaService;
        setWidthFull(); setPadding(true); setSpacing(true); getStyle().set("background", "#f8fafc");
        H2 titulo = new H2("Empresas"); titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nueva = boton(VaadinIcon.PLUS.create(), "Nueva empresa", "#16a34a", "white"); nueva.addClickListener(e -> abrirEmpresa(null));
        Button buscar = boton(VaadinIcon.SEARCH.create(), "Buscar", "#2563eb", "white"); buscar.addClickListener(e -> cargar());
        Button limpiar = boton(VaadinIcon.ERASER.create(), "Borrar", "#e2e8f0", "#1e293b"); limpiar.addClickListener(e -> { codigoFiltro.clear(); nombreFiltro.clear(); estadoFiltro.setValue("Todos"); cargar(); });
        HorizontalLayout acciones = new HorizontalLayout(nueva, buscar, limpiar);
        HorizontalLayout encabezado = new HorizontalLayout(titulo, acciones); encabezado.setWidthFull(); encabezado.setAlignItems(FlexComponent.Alignment.CENTER); encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        add(encabezado, filtros(), tablaEmpresas()); cargar();
    }

    @Override public void beforeEnter(BeforeEnterEvent event) { if (!esSuperadmin()) event.rerouteTo(DashboardView.class); }

    private VerticalLayout filtros() {
        codigoFiltro.setPlaceholder("Buscar por c\u00f3digo"); nombreFiltro.setPlaceholder("Buscar por nombre"); estadoFiltro.setItems("Todos", "Activas", "Inactivas"); estadoFiltro.setValue("Todos");
        HorizontalLayout fila = new HorizontalLayout(codigoFiltro, nombreFiltro, estadoFiltro); fila.setWidthFull(); fila.setFlexGrow(1, codigoFiltro, nombreFiltro); fila.getStyle().set("background", "white").set("padding", "1rem").set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        VerticalLayout contenedor = new VerticalLayout(fila); contenedor.setPadding(false); contenedor.setSpacing(false); return contenedor;
    }

    private VerticalLayout tablaEmpresas() {
        empresas.addColumn(Empresa::getCodigo).setHeader("C\u00d3DIGO").setWidth("150px").setFlexGrow(0);
        empresas.addColumn(Empresa::getNombre).setHeader("NOMBRE"); empresas.addColumn(Empresa::getTelefono).setHeader("TEL\u00c9FONO"); empresas.addColumn(Empresa::getEmail).setHeader("CORREO");
        empresas.addComponentColumn(empresa -> estado(empresa.getActiva())).setHeader("ESTADO").setWidth("130px").setFlexGrow(0);
        empresas.addComponentColumn(empresa -> { Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirEmpresa(empresa)); editar.setTooltipText("Editar"); editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY); return editar; }).setHeader("ACCIONES").setWidth("110px").setFlexGrow(0);
        empresas.setWidthFull(); empresas.setAllRowsVisible(true); return tarjeta(new H3("Empresas"), empresas);
    }

    private void abrirEmpresa(Empresa existente) {
        boolean edicion = existente != null; Dialog dialogo = new Dialog(); dialogo.setHeaderTitle(edicion ? "Editar empresa" : "Nueva empresa"); dialogo.setWidth("min(760px, 95vw)");
        TextField codigo = new TextField("C\u00f3digo"); TextField nombre = new TextField("Nombre"); TextField rnc = new TextField("RNC / Identificaci\u00f3n"); TextField telefono = new TextField("Tel\u00e9fono"); TextField correo = new TextField("Correo electr\u00f3nico"); TextArea direccion = new TextArea("Direcci\u00f3n"); direccion.setWidthFull();
        TextField sucursal = new TextField("Sucursal principal"); TextField adminUsuario = new TextField("Usuario administrador"); TextField adminNombre = new TextField("Nombre administrador"); TextField adminApellido = new TextField("Apellido administrador"); TextField adminCorreo = new TextField("Correo administrador"); PasswordField contrasena = new PasswordField("Contrase\u00f1a");
        sucursal.setRequiredIndicatorVisible(true); adminUsuario.setRequiredIndicatorVisible(true); adminNombre.setRequiredIndicatorVisible(true);
        adminApellido.setRequiredIndicatorVisible(true); adminCorreo.setRequiredIndicatorVisible(true); contrasena.setRequiredIndicatorVisible(true);
        adminUsuario.getElement().setAttribute("autocomplete", "new-password"); adminNombre.getElement().setAttribute("autocomplete", "new-password"); adminApellido.getElement().setAttribute("autocomplete", "new-password"); adminCorreo.getElement().setAttribute("autocomplete", "new-password"); contrasena.getElement().setAttribute("autocomplete", "new-password");
        if (edicion) { codigo.setValue(valor(existente.getCodigo())); nombre.setValue(valor(existente.getNombre())); rnc.setValue(valor(existente.getRncIdentificacion())); telefono.setValue(valor(existente.getTelefono())); correo.setValue(valor(existente.getEmail())); direccion.setValue(valor(existente.getDireccion())); }
        FormLayout datosEmpresa = new FormLayout(codigo, nombre, rnc, telefono, correo, direccion); datosEmpresa.setResponsiveSteps(new FormLayout.ResponsiveStep("0",1), new FormLayout.ResponsiveStep("620px",2)); datosEmpresa.setColspan(nombre, 2); datosEmpresa.setColspan(direccion,2); datosEmpresa.setWidthFull();
        VerticalLayout contenido = new VerticalLayout(new H3("Datos de la empresa"), datosEmpresa); contenido.setPadding(false); contenido.setSpacing(true);
        if (!edicion) { FormLayout alta = new FormLayout(sucursal, adminUsuario, adminNombre, adminApellido, adminCorreo, contrasena); alta.setResponsiveSteps(new FormLayout.ResponsiveStep("0",1), new FormLayout.ResponsiveStep("620px",2)); alta.setColspan(sucursal,2); contenido.add(new H3("Sucursal principal y administrador obligatorio"), alta); }
        else contenido.add(seccionAdministradores(existente));
        Button guardar = boton(VaadinIcon.DISC.create(), "Guardar", "#16a34a", "white"); guardar.addClickListener(e -> { try { if (edicion) empresaService.actualizar(existente.getId(), codigo.getValue(), nombre.getValue(), rnc.getValue(), telefono.getValue(), correo.getValue(), direccion.getValue()); else empresaService.crear(codigo.getValue(), nombre.getValue(), rnc.getValue(), telefono.getValue(), correo.getValue(), direccion.getValue(), sucursal.getValue(), adminUsuario.getValue(), adminNombre.getValue(), adminApellido.getValue(), adminCorreo.getValue(), contrasena.getValue()); cargar(); dialogo.close(); Notification.show("Empresa guardada.", 3000, Notification.Position.BOTTOM_START); } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE); } });
        Button cambiar = null; if (edicion) { boolean activa = Boolean.TRUE.equals(existente.getActiva()); cambiar = boton(activa ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), activa ? "Desactivar empresa" : "Activar empresa", activa ? "#dc2626" : "#16a34a", "white"); cambiar.addClickListener(e -> { empresaService.cambiarEstado(existente.getId(), !activa); cargar(); dialogo.close(); }); }
        Button cerrar = boton(VaadinIcon.CLOSE.create(), "Cerrar", "#e2e8f0", "#1e293b"); cerrar.addClickListener(e -> dialogo.close()); dialogo.add(contenido); dialogo.getFooter().add(guardar); if(cambiar != null) dialogo.getFooter().add(cambiar); dialogo.getFooter().add(cerrar); dialogo.open();
    }

    private VerticalLayout seccionAdministradores(Empresa empresa) {
        H3 titulo = new H3("Usuarios administradores"); titulo.getStyle().set("margin", "0");
        Button nuevo = boton(VaadinIcon.PLUS.create(), "Nuevo administrador", "#16a34a", "white"); nuevo.addClickListener(e -> abrirAdministrador(empresa));
        HorizontalLayout cabecera = new HorizontalLayout(titulo, nuevo); cabecera.setWidthFull(); cabecera.setAlignItems(FlexComponent.Alignment.CENTER); cabecera.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Grid<Usuario> tabla = new Grid<>(Usuario.class, false);
        tabla.addColumn(Usuario::getUsuario).setHeader("USUARIO"); tabla.addColumn(Usuario::getNombreCompleto).setHeader("NOMBRE"); tabla.addColumn(Usuario::getEmail).setHeader("CORREO");
        tabla.addComponentColumn(usuario -> estado(usuario.getActivo())).setHeader("ESTADO");
        tabla.addComponentColumn(usuario -> accionesAdministrador(empresa, usuario)).setHeader("ACCIONES").setWidth("150px").setFlexGrow(0);
        tabla.setItems(empresaService.usuarios(empresa.getId())); tabla.setWidthFull(); tabla.setAllRowsVisible(true);
        VerticalLayout seccion = new VerticalLayout(cabecera, tabla); seccion.setPadding(false); seccion.setSpacing(true); return seccion;
    }

    private HorizontalLayout accionesAdministrador(Empresa empresa, Usuario usuario) {
        Button restablecer = boton(VaadinIcon.KEY.create(), "Restablecer contraseña", "#2563eb", "white");
        restablecer.addClickListener(e -> abrirRestablecerContrasena(empresa, usuario));
        boolean activo = Boolean.TRUE.equals(usuario.getActivo());
        Button cambiarEstado = boton(activo ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), activo ? "Desactivar usuario" : "Activar usuario", activo ? "#dc2626" : "#16a34a", "white");
        cambiarEstado.addClickListener(e -> {
            boolean nuevoEstado = !Boolean.TRUE.equals(usuario.getActivo());
            empresaService.cambiarEstadoUsuario(empresa.getId(), usuario.getId(), nuevoEstado);
            usuario.setActivo(nuevoEstado);
            cambiarEstado.setIcon(nuevoEstado ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create());
            cambiarEstado.setTooltipText(nuevoEstado ? "Desactivar usuario" : "Activar usuario");
            cambiarEstado.getStyle().set("background", nuevoEstado ? "#dc2626" : "#16a34a");
            Notification.show(nuevoEstado ? "Usuario activado." : "Usuario desactivado.", 3000, Notification.Position.BOTTOM_START);
        });
        return new HorizontalLayout(restablecer, cambiarEstado);
    }

    private void abrirAdministrador(Empresa empresa) {
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("Nuevo administrador · " + empresa.getNombre()); dialogo.setWidth("min(620px, 95vw)"); TextField usuario = new TextField("Usuario"); TextField nombre = new TextField("Nombre"); TextField apellido = new TextField("Apellido"); TextField correo = new TextField("Correo electr\u00f3nico"); PasswordField contrasena = new PasswordField("Contrase\u00f1a"); usuario.getElement().setAttribute("autocomplete", "new-password"); nombre.getElement().setAttribute("autocomplete", "new-password"); apellido.getElement().setAttribute("autocomplete", "new-password"); correo.getElement().setAttribute("autocomplete", "new-password"); contrasena.getElement().setAttribute("autocomplete", "new-password"); FormLayout formulario = new FormLayout(usuario,nombre,apellido,correo,contrasena); formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0",1),new FormLayout.ResponsiveStep("560px",2)); formulario.setColspan(contrasena,2);
        Button guardar = boton(VaadinIcon.DISC.create(), "Guardar", "#16a34a", "white"); guardar.addClickListener(e -> { try { empresaService.crearAdministrador(empresa.getId(), usuario.getValue(), nombre.getValue(), apellido.getValue(), correo.getValue(), contrasena.getValue()); dialogo.close(); Notification.show("Administrador creado. Cierra y vuelve a abrir la empresa para verlo en la tabla.",3500,Notification.Position.BOTTOM_START); } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE); }}); Button cerrar = boton(VaadinIcon.CLOSE.create(), "Cerrar", "#e2e8f0", "#1e293b"); cerrar.addClickListener(e -> dialogo.close()); dialogo.add(formulario); dialogo.getFooter().add(guardar,cerrar); dialogo.open();
    }

    private void abrirRestablecerContrasena(Empresa empresa, Usuario usuario) {
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("Restablecer contraseña · " + usuario.getUsuario()); dialogo.setWidth("min(480px, 95vw)");
        PasswordField contrasena = new PasswordField("Nueva contraseña"); PasswordField confirmacion = new PasswordField("Confirmar contraseña"); contrasena.getElement().setAttribute("autocomplete", "new-password"); confirmacion.getElement().setAttribute("autocomplete", "new-password");
        FormLayout formulario = new FormLayout(contrasena, confirmacion); formulario.setWidthFull();
        Button guardar = boton(VaadinIcon.DISC.create(), "Guardar", "#16a34a", "white"); guardar.addClickListener(e -> { try { empresaService.restablecerContrasena(empresa.getId(), usuario.getId(), contrasena.getValue(), confirmacion.getValue()); dialogo.close(); Notification.show("Contraseña restablecida.", 3000, Notification.Position.BOTTOM_START); } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE); } });
        Button cerrar = boton(VaadinIcon.CLOSE.create(), "Cerrar", "#e2e8f0", "#1e293b"); cerrar.addClickListener(e -> dialogo.close()); dialogo.add(formulario); dialogo.getFooter().add(guardar, cerrar); dialogo.open();
    }

    private void cargar() { Boolean activa = null; if ("Activas".equals(estadoFiltro.getValue())) activa = Boolean.TRUE; if ("Inactivas".equals(estadoFiltro.getValue())) activa = Boolean.FALSE; empresas.setItems(empresaService.buscar(codigoFiltro.getValue(), nombreFiltro.getValue(), activa)); }
    private boolean esSuperadmin() { var usuario = AuthService.getAuthenticatedUser(); return usuario != null && usuario.getAuthorities().stream().anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority())); }
    private VerticalLayout tarjeta(Component... contenido) { VerticalLayout tarjeta = new VerticalLayout(contenido); tarjeta.setWidthFull(); tarjeta.getStyle().set("background","white").set("border","1px solid #e2e8f0").set("border-radius","12px").set("padding","1.5rem"); return tarjeta; }
    private Button boton(Component icono,String ayuda,String fondo,String color) { Button boton = new Button(icono); boton.setTooltipText(ayuda); boton.getStyle().set("background",fondo).set("color",color); return boton; }
    private Span estado(Boolean activo) { Span etiqueta = new Span(Boolean.TRUE.equals(activo) ? "Activa" : "Inactiva"); etiqueta.addClassName(Boolean.TRUE.equals(activo)?"badge-activo":"badge-inactivo"); return etiqueta; }
    private String valor(String texto) { return texto == null ? "" : texto; }
}
