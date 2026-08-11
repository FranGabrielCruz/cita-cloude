package com.citacloud.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración del Tenant | CitaCloud")
@PermitAll
public class ConfiguracionView extends VerticalLayout {

    public ConfiguracionView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        // Header Section
        H2 title = new H2("Configuración del Tenant");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Span subtitle = new Span("Parámetros principales y sucursales de la institución.");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "0.875rem");

        Span tenantBadge = new Span("⚡ Multitenant Activo  |  ID: T-48291");
        tenantBadge.getStyle()
                .set("background-color", "#eff6ff")
                .set("color", "#1565D8")
                .set("border", "1px solid #bfdbfe")
                .set("padding", "0.375rem 0.875rem")
                .set("border-radius", "8px")
                .set("font-weight", "600")
                .set("font-size", "0.8125rem");

        HorizontalLayout headerRow = new HorizontalLayout(new Div(title, subtitle), tenantBadge);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Top 2-Column Split
        HorizontalLayout topSplit = new HorizontalLayout();
        topSplit.setWidthFull();
        topSplit.setSpacing(true);

        // Left Card: Datos de la Institución
        VerticalLayout instCard = new VerticalLayout();
        instCard.setWidth("65%");
        instCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        HorizontalLayout instHeader = new HorizontalLayout(new H3("Datos de la Institución"), new Button("Editar Datos"));
        instHeader.setWidthFull();
        instHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        instHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField nameField = new TextField("Nombre de la Institución");
        nameField.setValue("Clínica San Rafael");
        nameField.setWidthFull();

        TextField rncField = new TextField("RNC / Identificación");
        rncField.setValue("101-99887-1");
        rncField.setWidth("48%");

        TextField telField = new TextField("Teléfono Principal");
        telField.setValue("+1 (809) 555-0192");
        telField.setWidth("48%");

        HorizontalLayout row2 = new HorizontalLayout(rncField, telField);
        row2.setWidthFull();

        TextField dirField = new TextField("Dirección");
        dirField.setValue("Av. Ensanche Clínico, Santo Domingo");
        dirField.setWidthFull();

        ComboBox<String> tzField = new ComboBox<>("Zona Horaria");
        tzField.setItems("Santo Domingo (AST -04:00)", "Nueva York (EST -05:00)");
        tzField.setValue("Santo Domingo (AST -04:00)");
        tzField.setWidthFull();

        instCard.add(instHeader, nameField, row2, dirField, tzField);

        // Right Column: Logotipo + Resumen de Capacidad
        VerticalLayout rightCol = new VerticalLayout();
        rightCol.setWidth("35%");
        rightCol.setSpacing(true);

        // Card 1: Logotipo
        VerticalLayout logoCard = new VerticalLayout();
        logoCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.25rem")
                .set("align-items", "center");

        H3 logoTitle = new H3("Logotipo");
        logoTitle.getStyle().set("margin", "0 0 0.5rem 0").set("align-self", "flex-start");

        Div logoBox = new Div(VaadinIcon.BUILDING.create());
        logoBox.getStyle()
                .set("width", "100px")
                .set("height", "80px")
                .set("border", "2px dashed #cbd5e1")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "#94a3b8")
                .set("font-size", "2rem");

        Button logoBtn = new Button("Cambiar Logo");
        logoBtn.setWidthFull();

        logoCard.add(logoTitle, logoBox, logoBtn);

        // Card 2: Resumen de Capacidad (Blue Card)
        VerticalLayout capacityCard = new VerticalLayout();
        capacityCard.getStyle()
                .set("background", "linear-gradient(135deg, #1565D8 0%, #0d47a1 100%)")
                .set("color", "#ffffff")
                .set("border-radius", "12px")
                .set("padding", "1.25rem");

        H3 capTitle = new H3("Resumen de Capacidad");
        capTitle.getStyle().set("margin", "0 0 1rem 0").set("color", "#ffffff");

        HorizontalLayout s1 = new HorizontalLayout(new Span("Sucursales Activas"), new Span("4 / 10"));
        s1.setWidthFull();
        s1.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        ProgressBar pb1 = new ProgressBar();
        pb1.setValue(0.4);

        HorizontalLayout s2 = new HorizontalLayout(new Span("Usuarios Límite"), new Span("120 / 500"));
        s2.setWidthFull();
        s2.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        ProgressBar pb2 = new ProgressBar();
        pb2.setValue(0.24);

        capacityCard.add(capTitle, s1, pb1, s2, pb2);

        rightCol.add(logoCard, capacityCard);
        topSplit.add(instCard, rightCol);

        // Bottom Card: Sucursales Vinculadas
        VerticalLayout botCard = new VerticalLayout();
        botCard.setWidthFull();
        botCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        HorizontalLayout botHeader = new HorizontalLayout(new H3("Sucursales Vinculadas"), new Button("Nueva Sucursal", VaadinIcon.PLUS.create()));
        botHeader.setWidthFull();
        botHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        botHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<String[]> sucGrid = new Grid<>();
        sucGrid.addColumn(s -> s[0]).setHeader("CÓDIGO");
        sucGrid.addColumn(s -> s[1]).setHeader("UBICACIÓN");
        sucGrid.addComponentColumn(s -> {
            Span chip = new Span("Activa");
            chip.addClassName("badge-activo");
            return chip;
        }).setHeader("ESTADO");

        sucGrid.setItems(
                new String[]{"SUC-01", "Santo Domingo Central"},
                new String[]{"SUC-02", "Santiago Norte"}
        );

        botCard.add(botHeader, sucGrid);

        add(headerRow, topSplit, botCard);
    }
}
