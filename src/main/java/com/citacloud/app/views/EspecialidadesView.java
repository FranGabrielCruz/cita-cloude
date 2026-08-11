package com.citacloud.app.views;

import com.citacloud.app.models.Especialidad;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EspecialidadService;
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

@Route(value = "especialidades", layout = MainLayout.class)
@PageTitle("Especialidades | CitaCloud")
@PermitAll
public class EspecialidadesView extends VerticalLayout {

    private final EspecialidadService especialidadService;

    public EspecialidadesView(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        H2 title = new H2("Especialidades Médicas");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevaBtn = new Button("Nueva Especialidad", VaadinIcon.PLUS.create());
        nuevaBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevaBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevaBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<Especialidad> grid = new Grid<>(Especialidad.class, false);
        grid.addColumn(Especialidad::getNombre).setHeader("NOMBRE");
        grid.addColumn(Especialidad::getDescripcion).setHeader("DESCRIPCIÓN");
        grid.addComponentColumn(e -> {
            Span chip = new Span(Boolean.TRUE.equals(e.getActiva()) ? "Activa" : "Inactiva");
            chip.addClassName(Boolean.TRUE.equals(e.getActiva()) ? "badge-activo" : "badge-inactivo");
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(especialidadService.listarPorEmpresa(empresaId));
        }

        add(headerRow, grid);
    }
}
