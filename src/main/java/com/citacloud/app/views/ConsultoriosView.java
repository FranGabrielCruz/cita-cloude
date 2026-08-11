package com.citacloud.app.views;

import com.citacloud.app.models.Consultorio;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.ConsultorioService;
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

@Route(value = "consultorios", layout = MainLayout.class)
@PageTitle("Consultorios | CitaCloud")
@PermitAll
public class ConsultoriosView extends VerticalLayout {

    private final ConsultorioService consultorioService;

    public ConsultoriosView(ConsultorioService consultorioService) {
        this.consultorioService = consultorioService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        H2 title = new H2("Consultorios");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevoBtn = new Button("Nuevo Consultorio", VaadinIcon.PLUS.create());
        nuevoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevoBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<Consultorio> grid = new Grid<>(Consultorio.class, false);
        grid.addColumn(Consultorio::getCodigo).setHeader("CÓDIGO");
        grid.addColumn(Consultorio::getNombre).setHeader("NOMBRE");
        grid.addColumn(c -> c.getSucursal() != null ? c.getSucursal().getNombre() : "Sucursal Principal").setHeader("SUCURSAL");
        grid.addColumn(Consultorio::getUbicacion).setHeader("UBICACIÓN");
        grid.addComponentColumn(c -> {
            Span chip = new Span(Boolean.TRUE.equals(c.getActivo()) ? "Activo" : "Inactivo");
            chip.addClassName(Boolean.TRUE.equals(c.getActivo()) ? "badge-activo" : "badge-inactivo");
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(consultorioService.listarPorEmpresa(empresaId));
        }

        add(headerRow, grid);
    }
}
