package com.citacloud.app.views;

import com.citacloud.app.models.Permiso;
import com.citacloud.app.models.Rol;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.RolService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.List;
import java.util.UUID;

@Route(value = "roles", layout = MainLayout.class)
@PageTitle("Roles | CitaCloud")
@PermitAll
@CssImport("./styles/mobile-layouts.css")
public class RolesView extends VerticalLayout {
    private final RolService rolService; private final UUID empresaId; private final Grid<Rol> grid = new Grid<>(Rol.class, false); private final PaginadorTabla<Rol> paginador = new PaginadorTabla<>(grid);
    private final TextField nombreFiltro = new TextField("Nombre"); private final ComboBox<String> estadoFiltro = new ComboBox<>("Estado");
    public RolesView(RolService rolService) { this.rolService = rolService; TenantUserDetails sesion = AuthService.getAuthenticatedUser(); empresaId = sesion == null ? null : sesion.getEmpresaId(); setSizeFull(); setPadding(true); setSpacing(true); configurarFiltros(); configurarTabla(); add(encabezado(), filtros(), grid, paginador); actualizar(); }
    private void configurarFiltros() { nombreFiltro.setPlaceholder("Buscar por nombre"); nombreFiltro.setPrefixComponent(VaadinIcon.SEARCH.create()); estadoFiltro.setItems("Activo", "Inactivo"); estadoFiltro.setPlaceholder("Seleccione estado"); }
    private HorizontalLayout encabezado() { H2 titulo = new H2("Roles"); titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800"); Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> formulario(null)); nuevo.setTooltipText("Nuevo rol"); nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY); nuevo.getStyle().set("background", "#16a34a").set("color", "white"); Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizar()); buscar.setTooltipText("Buscar"); buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> { nombreFiltro.clear(); estadoFiltro.clear(); actualizar(); }); limpiar.setTooltipText("Limpiar"); limpiar.getStyle().set("background", "#e2e8f0").set("color", "#334155"); HorizontalLayout acciones = new HorizontalLayout(nuevo,buscar,limpiar); acciones.setSpacing(false); acciones.getStyle().set("gap","0.35rem"); HorizontalLayout fila = new HorizontalLayout(titulo,acciones); fila.setWidthFull(); fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); fila.setAlignItems(FlexComponent.Alignment.CENTER); return fila; }
    private HorizontalLayout filtros() { HorizontalLayout filtros = new HorizontalLayout(nombreFiltro,estadoFiltro); filtros.addClassName("mobile-stacked-filters"); filtros.setWidthFull(); filtros.setFlexGrow(1,nombreFiltro,estadoFiltro); filtros.getStyle().set("background","#fff").set("padding","1rem").set("border-radius","12px").set("border","1px solid #e2e8f0"); return filtros; }
    private void configurarTabla() { grid.addColumn(Rol::getNombre).setHeader("ROL"); grid.addColumn(Rol::getDescripcion).setHeader("DESCRIPCI\u00d3N"); grid.addComponentColumn(rol -> { Span estado = new Span(Boolean.TRUE.equals(rol.getActivo()) ? "Activo" : "Inactivo"); estado.addClassName(Boolean.TRUE.equals(rol.getActivo()) ? "badge-activo" : "badge-inactivo"); return estado; }).setHeader("ESTADO").setWidth("120px").setFlexGrow(0); grid.addComponentColumn(rol -> { Button editar = new Button(VaadinIcon.EDIT.create(), e -> formulario(rol)); editar.setTooltipText("Editar"); editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY); return editar; }).setHeader("ACCIONES").setWidth("115px").setFlexGrow(0); grid.setWidthFull(); grid.setAllRowsVisible(true); }
    private void actualizar() { if (empresaId == null) { paginador.setItems(List.of()); return; } Boolean activo = "Activo".equals(estadoFiltro.getValue()) ? Boolean.TRUE : "Inactivo".equals(estadoFiltro.getValue()) ? Boolean.FALSE : null; paginador.setItems(rolService.buscar(empresaId,nombreFiltro.getValue(),activo)); }
    private void formulario(Rol existente) { if (empresaId == null) { Notification.show("No se pudo identificar la empresa de la sesi\u00f3n."); return; } boolean edicion = existente != null; Dialog dialog = new Dialog(); dialog.setHeaderTitle(edicion ? "Editar Rol" : "Nuevo Rol"); dialog.setWidth("650px"); TextField nombre = new TextField("Nombre del rol"); nombre.setRequiredIndicatorVisible(true); TextArea descripcion = new TextArea("Descripci\u00f3n"); descripcion.setWidthFull(); CheckboxGroup<Permiso> permisos = new CheckboxGroup<>("Opciones visibles en el men\u00fa"); TenantUserDetails sesion=AuthService.getAuthenticatedUser(); boolean perfilSuperadmin=edicion&&"SUPERADMIN".equalsIgnoreCase(existente.getNombre())&&sesion!=null&&"SUPERADMIN".equalsIgnoreCase(sesion.getEmpresaCodigo())&&sesion.getAuthorities().stream().anyMatch(a->"ROLE_SUPERADMIN".equals(a.getAuthority())); List<Permiso> opcionesMenu = rolService.permisosMenu().stream().filter(opcion->perfilSuperadmin||(!opcion.getCodigo().toUpperCase().contains("EMPRESA")&&!opcion.getNombre().toUpperCase().contains("EMPRESA"))).toList(); permisos.setItems(opcionesMenu); permisos.setItemLabelGenerator(Permiso::getNombre); if (edicion) { nombre.setValue(existente.getNombre()); descripcion.setValue(existente.getDescripcion() == null ? "" : existente.getDescripcion()); java.util.Set<UUID> permisosGuardados = existente.getPermisos().stream().map(Permiso::getId).collect(java.util.stream.Collectors.toSet()); permisos.setValue(opcionesMenu.stream().filter(opcion -> permisosGuardados.contains(opcion.getId())).collect(java.util.stream.Collectors.toSet())); } FormLayout datos = new FormLayout(nombre,descripcion); datos.setColspan(descripcion,2); dialog.add(datos,permisos); Button guardar = new Button(VaadinIcon.DISC.create(), e -> { try { if(edicion) rolService.actualizar(empresaId,existente.getId(),nombre.getValue(),descripcion.getValue(),permisos.getValue()); else rolService.crear(empresaId,nombre.getValue(),descripcion.getValue(),permisos.getValue()); dialog.close(); actualizar(); Notification.show(edicion ? "Rol actualizado." : "Rol creado.",3000,Notification.Position.BOTTOM_START); } catch(IllegalArgumentException ex) { Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE); } }); guardar.setTooltipText("Guardar"); guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); guardar.getStyle().set("background","#16a34a").set("color","white"); Button cerrar = new Button(VaadinIcon.CLOSE.create(),e->dialog.close()); cerrar.setTooltipText("Cerrar"); cerrar.getStyle().set("background","#e2e8f0").set("color","#1e293b"); dialog.getFooter().add(guardar); if(edicion) { boolean activo=Boolean.TRUE.equals(existente.getActivo()); Button estado=new Button(activo?VaadinIcon.BAN.create():VaadinIcon.CHECK.create(),e->{ try { rolService.cambiarEstado(empresaId,existente.getId(),!activo); dialog.close(); actualizar(); Notification.show(activo?"Rol desactivado.":"Rol activado.",3000,Notification.Position.BOTTOM_START); } catch(IllegalArgumentException ex){Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE);} }); estado.setTooltipText(activo?"Desactivar rol":"Activar rol"); estado.getStyle().set("background",activo?"#dc2626":"#16a34a").set("color","white"); dialog.getFooter().add(estado); } dialog.getFooter().add(cerrar); dialog.open(); }
}
