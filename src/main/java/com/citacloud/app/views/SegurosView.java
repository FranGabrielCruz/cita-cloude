package com.citacloud.app.views;

import com.citacloud.app.models.Aseguradora;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.AseguradoraService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.UUID;

@Route(value = "seguros", layout = MainLayout.class)
@PageTitle("Seguros | CitaCloud")
@PermitAll
public class SegurosView extends VerticalLayout {

    private final AseguradoraService aseguradoraService;
    private final UUID empresaId;
    private final Grid<Aseguradora> grid = new Grid<>(Aseguradora.class, false);
    private final PaginadorTabla<Aseguradora> paginador = new PaginadorTabla<>(grid);
    private final TextField nombreFiltro = new TextField("Nombre");
    private final ComboBox<String> estadoFiltro = new ComboBox<>("Estado");

    public SegurosView(AseguradoraService aseguradoraService) {
        this.aseguradoraService = aseguradoraService;
        TenantUserDetails user = AuthService.getAuthenticatedUser();
        empresaId = user == null ? null : user.getEmpresaId();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        configurarFiltros();
        configurarTabla();
        add(crearEncabezado(), crearBarraFiltros(), grid, paginador);
        actualizarAseguradoras();
    }

    private void configurarFiltros() {
        nombreFiltro.setPlaceholder("Buscar por nombre");
        nombreFiltro.setPrefixComponent(VaadinIcon.SEARCH.create());
        estadoFiltro.setItems("Activo", "Inactivo");
        estadoFiltro.setPlaceholder("Seleccione estado");
    }

    private HorizontalLayout crearEncabezado() {
        H2 titulo = new H2("Seguros");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevo = new Button(VaadinIcon.PLUS.create(), event -> abrirFormulario(null));
        nuevo.setTooltipText("Nueva aseguradora");
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        Button buscar = new Button(VaadinIcon.SEARCH.create(), event -> actualizarAseguradoras());
        buscar.setTooltipText("Buscar");
        buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), event -> limpiarFiltros());
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
        HorizontalLayout filtros = new HorizontalLayout(nombreFiltro, estadoFiltro);
        filtros.setWidthFull();
        filtros.setFlexGrow(1, nombreFiltro, estadoFiltro);
        filtros.getStyle().set("flex-wrap", "wrap").set("background-color", "#ffffff").set("padding", "1rem")
                .set("border-radius", "12px").set("border", "1px solid #e2e8f0");
        return filtros;
    }

    private void configurarTabla() {
        grid.addColumn(Aseguradora::getNombre).setHeader("ASEGURADORA");
        grid.addColumn(Aseguradora::getTelefono).setHeader("TEL\u00c9FONO");
        grid.addComponentColumn(aseguradora -> {
            Span estado = new Span(Boolean.TRUE.equals(aseguradora.getActiva()) ? "Activa" : "Inactiva");
            estado.addClassName(Boolean.TRUE.equals(aseguradora.getActiva()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO").setWidth("125px").setFlexGrow(0);
        grid.addComponentColumn(aseguradora -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), event -> abrirFormulario(aseguradora));
            editar.setTooltipText("Editar");
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES").setWidth("115px").setFlexGrow(0);
        grid.setWidthFull();
    }

    private void actualizarAseguradoras() {
        if (empresaId == null) {
            paginador.setItems(List.of());
            return;
        }
        Boolean activa = "Activo".equals(estadoFiltro.getValue()) ? Boolean.TRUE
                : "Inactivo".equals(estadoFiltro.getValue()) ? Boolean.FALSE : null;
        paginador.setItems(aseguradoraService.buscar(empresaId, nombreFiltro.getValue(), activa));
    }

    private void limpiarFiltros() {
        nombreFiltro.clear();
        estadoFiltro.clear();
        actualizarAseguradoras();
    }

    private void abrirFormulario(Aseguradora existente) {
        if (empresaId == null) {
            Notification.show("No se pudo identificar la empresa de la sesi\u00f3n.", 3000, Notification.Position.MIDDLE);
            return;
        }
        boolean esEdicion = existente != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Aseguradora" : "Nueva Aseguradora");
        dialog.setWidth("560px");
        TextField nombre = new TextField("Nombre");
        nombre.setRequiredIndicatorVisible(true);
        nombre.setMaxLength(100);
        TextField telefono = new TextField("Tel\u00e9fono");
        telefono.setMaxLength(30);
        if (esEdicion) {
            nombre.setValue(existente.getNombre());
            telefono.setValue(existente.getTelefono() == null ? "" : existente.getTelefono());
        }
        FormLayout formulario = new FormLayout(nombre, telefono);
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("420px", 2));
        dialog.add(formulario);

        Button guardar = new Button(VaadinIcon.DISC.create(), event -> {
            try {
                if (esEdicion) {
                    aseguradoraService.actualizar(empresaId, existente.getId(), nombre.getValue(), telefono.getValue());
                } else {
                    aseguradoraService.crear(empresaId, nombre.getValue(), telefono.getValue());
                }
                dialog.close();
                actualizarAseguradoras();
                Notification.show(esEdicion ? "Aseguradora actualizada." : "Aseguradora creada.",
                        3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        guardar.setTooltipText("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");

        Button cerrar = new Button(VaadinIcon.CLOSE.create(), event -> dialog.close());
        cerrar.setTooltipText("Cerrar");
        cerrar.getStyle().set("background-color", "#e2e8f0").set("color", "#1e293b");
        dialog.getFooter().add(guardar);
        if (esEdicion) {
            boolean activa = Boolean.TRUE.equals(existente.getActiva());
            Button cambiarEstado = new Button(activa ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), event -> {
                try {
                    aseguradoraService.cambiarEstado(empresaId, existente.getId(), !activa);
                    dialog.close();
                    actualizarAseguradoras();
                    Notification.show(activa ? "Aseguradora desactivada." : "Aseguradora activada.",
                            3000, Notification.Position.BOTTOM_START);
                } catch (IllegalArgumentException exception) {
                    Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            cambiarEstado.setTooltipText(activa ? "Desactivar aseguradora" : "Activar aseguradora");
            cambiarEstado.getStyle().set("background-color", activa ? "#dc2626" : "#16a34a").set("color", "#ffffff");
            dialog.getFooter().add(cambiarEstado);
        }
        dialog.getFooter().add(cerrar);
        dialog.open();
    }
}
