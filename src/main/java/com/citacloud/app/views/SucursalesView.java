package com.citacloud.app.views;

import com.citacloud.app.models.Sucursal;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.SucursalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import java.util.UUID;

@Route(value = "sucursales", layout = MainLayout.class)
@PageTitle("Sucursales | CitaCloud")
@PermitAll
public class SucursalesView extends VerticalLayout {

    private final SucursalService sucursalService;

    public SucursalesView(SucursalService sucursalService) {
        this.sucursalService = sucursalService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        H2 title = new H2("Sucursales");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevaBtn = new Button("Nueva Sucursal", VaadinIcon.PLUS.create());
        nuevaBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevaBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevaBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<Sucursal> grid = new Grid<>(Sucursal.class, false);
        grid.addColumn(Sucursal::getCodigo).setHeader("CÓDIGO");
        grid.addColumn(Sucursal::getNombre).setHeader("NOMBRE");
        grid.addColumn(Sucursal::getTelefono).setHeader("TELÉFONO");
        grid.addColumn(Sucursal::getDireccion).setHeader("DIRECCIÓN");
        grid.addComponentColumn(s -> {
            Span chip = new Span(Boolean.TRUE.equals(s.getActiva()) ? "Activa" : "Inactiva");
            chip.addClassName(Boolean.TRUE.equals(s.getActiva()) ? "badge-activo" : "badge-inactivo");
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(sucursalService.listarPorEmpresa(empresaId));
        }

        add(headerRow, grid);
    }
}
