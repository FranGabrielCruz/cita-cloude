package com.citacloud.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "horarios", layout = MainLayout.class)
@PageTitle("Horarios Médicos | CitaCloud")
@PermitAll
public class HorariosView extends VerticalLayout {

    public HorariosView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Horarios Médicos y Ausencias");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevoHorarioBtn = new Button("Configurar Horario", VaadinIcon.PLUS.create());
        nuevoHorarioBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoHorarioBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevoHorarioBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Paragraph info = new Paragraph("Gestión de turnos de trabajo, horarios semanales por médico y sucursal, y registro de ausencias.");
        info.getStyle().set("color", "#64748b");

        add(headerRow, info);
    }
}
