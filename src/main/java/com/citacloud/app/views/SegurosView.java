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

@Route(value = "seguros", layout = MainLayout.class)
@PageTitle("Seguros de Salud | CitaCloud")
@PermitAll
public class SegurosView extends VerticalLayout {

    public SegurosView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Aseguradoras y Planes");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevaAseguradoraBtn = new Button("Nueva Aseguradora", VaadinIcon.PLUS.create());
        nuevaAseguradoraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevaAseguradoraBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevaAseguradoraBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Paragraph info = new Paragraph("Administración de ARS, aseguradoras privadas y cobertura de pólizas para pacientes.");
        info.getStyle().set("color", "#64748b");

        add(headerRow, info);
    }
}
