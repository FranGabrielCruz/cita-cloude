package com.citacloud.app.views;

import com.citacloud.app.models.Especialidad;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EspecialidadService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "especialidades", layout = MainLayout.class)
@PageTitle("Especialidades | CitaCloud")
@PermitAll
public class EspecialidadesView extends VerticalLayout {

    private final EspecialidadService especialidadService;
    private final UUID empresaId;
    private final Grid<Especialidad> grid = new Grid<>(Especialidad.class, false);
    private final PaginadorTabla<Especialidad> paginador = new PaginadorTabla<>(grid);
    private final TextField nombreFiltro = new TextField("Nombre");

    public EspecialidadesView(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
        TenantUserDetails user = AuthService.getAuthenticatedUser();
        this.empresaId = user == null ? null : user.getEmpresaId();
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        nombreFiltro.setPlaceholder("Buscar por nombre");
        nombreFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        configurarTabla();
        add(crearEncabezado(), crearBarraFiltros(), grid, paginador);
        actualizarEspecialidades();
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Especialidades");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> abrirFormulario(null));
        nuevo.setTooltipText("Nueva especialidad");
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizarEspecialidades());
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> {
            nombreFiltro.clear();
            actualizarEspecialidades();
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
        return encabezado;
    }

    private HorizontalLayout crearBarraFiltros() {
        HorizontalLayout filtros = new HorizontalLayout(nombreFiltro);
        filtros.setWidthFull();
        filtros.setFlexGrow(1, nombreFiltro);
        filtros.getStyle().set("background-color", "#ffffff").set("padding", "1rem")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        return filtros;
    }

    private void configurarTabla() {
        grid.addColumn(Especialidad::getNombre).setHeader("NOMBRE");
        grid.addColumn(Especialidad::getDescripcion).setHeader("DESCRIPCI\u00d3N");
        grid.addComponentColumn(especialidad -> {
            Span estado = new Span(Boolean.TRUE.equals(especialidad.getActiva()) ? "Activa" : "Inactiva");
            estado.addClassName(Boolean.TRUE.equals(especialidad.getActiva()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(especialidad -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirFormulario(especialidad));
            editar.setTooltipText("Editar especialidad");
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES").setWidth("120px").setFlexGrow(0);
        grid.setWidthFull();
    }

    private void actualizarEspecialidades() {
        paginador.setItems(empresaId == null ? List.of() : especialidadService.buscar(empresaId, nombreFiltro.getValue()));
    }

    private void abrirFormulario(Especialidad especialidadExistente) {
        if (empresaId == null) {
            Notification.show("No se pudo identificar la empresa de la sesi\u00f3n.", 3000, Notification.Position.MIDDLE);
            return;
        }
        boolean esEdicion = especialidadExistente != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Especialidad" : "Nueva Especialidad");
        dialog.setWidth("560px");
        TextField nombre = new TextField("Nombre");
        nombre.setRequiredIndicatorVisible(true);
        nombre.setMaxLength(100);
        TextArea descripcion = new TextArea("Descripci\u00f3n");
        descripcion.setMaxLength(2000);
        descripcion.setWidthFull();
        if (esEdicion) {
            nombre.setValue(especialidadExistente.getNombre());
            descripcion.setValue(especialidadExistente.getDescripcion() == null ? "" : especialidadExistente.getDescripcion());
        }
        dialog.add(new FormLayout(nombre, descripcion));

        Button guardar = new Button(VaadinIcon.DISC.create(), e -> {
            try {
                if (esEdicion) {
                    especialidadService.actualizar(empresaId, especialidadExistente.getId(), nombre.getValue(), descripcion.getValue());
                } else {
                    Especialidad especialidad = new Especialidad();
                    especialidad.setEmpresaId(empresaId);
                    especialidad.setNombre(nombre.getValue());
                    especialidad.setDescripcion(descripcion.getValue());
                    especialidadService.guardar(especialidad);
                }
                dialog.close();
                actualizarEspecialidades();
                Notification.show(esEdicion ? "Especialidad actualizada." : "Especialidad creada.",
                        3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        guardar.setTooltipText("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button cerrar = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cerrar.setTooltipText("Cerrar");
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        dialog.getFooter().add(guardar);
        if (esEdicion) {
            boolean activa = Boolean.TRUE.equals(especialidadExistente.getActiva());
            Button cambiarEstado = new Button(activa ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), e -> {
                try {
                    especialidadService.cambiarEstado(empresaId, especialidadExistente.getId(), !activa);
                    dialog.close();
                    actualizarEspecialidades();
                    Notification.show(activa ? "Especialidad desactivada." : "Especialidad activada.",
                            3000, Notification.Position.BOTTOM_START);
                } catch (IllegalArgumentException exception) {
                    Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            cambiarEstado.setTooltipText(activa ? "Desactivar especialidad" : "Activar especialidad");
            cambiarEstado.getStyle().set("background-color", activa ? "#dc2626" : "#16a34a").set("color", "#ffffff");
            dialog.getFooter().add(cambiarEstado);
        }
        dialog.getFooter().add(cerrar);
        dialog.open();
    }
}
