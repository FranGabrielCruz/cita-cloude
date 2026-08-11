package com.citacloud.app.views;

import com.citacloud.app.models.Medico;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.MedicoService;
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

@Route(value = "medicos", layout = MainLayout.class)
@PageTitle("Médicos | CitaCloud")
@PermitAll
public class MedicosView extends VerticalLayout {

    private final MedicoService medicoService;

    public MedicosView(MedicoService medicoService) {
        this.medicoService = medicoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        H2 title = new H2("Médicos");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevoMedicoBtn = new Button("Nuevo Médico", VaadinIcon.PLUS.create());
        nuevoMedicoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoMedicoBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevoMedicoBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<Medico> grid = new Grid<>(Medico.class, false);
        grid.addColumn(Medico::getCodigo).setHeader("CÓDIGO");
        grid.addColumn(Medico::getNombreCompleto).setHeader("NOMBRE");
        grid.addColumn(Medico::getExequatur).setHeader("EXEQUÁTUR");
        grid.addColumn(Medico::getEspecialidadesTexto).setHeader("ESPECIALIDADES");
        grid.addColumn(Medico::getTelefono).setHeader("TELÉFONO");
        grid.addColumn(Medico::getEmail).setHeader("EMAIL");
        grid.addComponentColumn(m -> {
            Span chip = new Span(Boolean.TRUE.equals(m.getActivo()) ? "Activo" : "Inactivo");
            chip.addClassName(Boolean.TRUE.equals(m.getActivo()) ? "badge-activo" : "badge-inactivo");
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(medicoService.listarPorEmpresa(empresaId));
        }

        add(headerRow, grid);
    }
}
