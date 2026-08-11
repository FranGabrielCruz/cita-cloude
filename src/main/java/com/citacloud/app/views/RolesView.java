package com.citacloud.app.views;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "roles", layout = MainLayout.class)
@PageTitle("Gestión de Roles y Permisos | CitaCloud")
@PermitAll
public class RolesView extends VerticalLayout {

    public RolesView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        // Title
        H2 title = new H2("Gestión de Roles y Permisos");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Paragraph subtitle = new Paragraph("Configura los niveles de acceso para los usuarios de tu organización.");
        subtitle.getStyle().set("color", "#64748b").set("margin", "0 0 1rem 0");

        // Alert Banner (matching desing/roles.png)
        Div alertBanner = new Div();
        alertBanner.getStyle()
                .set("background-color", "#eff6ff")
                .set("border", "1px solid #bfdbfe")
                .set("border-radius", "12px")
                .set("padding", "1rem 1.25rem")
                .set("display", "flex")
                .set("align-items", "flex-start")
                .set("gap", "0.75rem")
                .set("width", "100%");

        Div infoIcon = new Div(VaadinIcon.INFO_CIRCLE.create());
        infoIcon.getStyle().set("color", "#2563eb").set("font-size", "1.25rem");

        Div alertText = new Div();
        H4 alertTitle = new H4("Alcance de los permisos");
        alertTitle.getStyle().set("margin", "0 0 0.25rem 0").set("color", "#1e3a8a").set("font-size", "0.9375rem");
        Paragraph alertBody = new Paragraph("Los permisos configurados en esta sección se aplican de manera exclusiva dentro del entorno de la empresa actual (Clínica San Rafael). No afectan el acceso a otras organizaciones.");
        alertBody.getStyle().set("margin", "0").set("color", "#1e40af").set("font-size", "0.84375rem");
        alertText.add(alertTitle, alertBody);

        alertBanner.add(infoIcon, alertText);

        // 2-Column Split
        HorizontalLayout mainSplit = new HorizontalLayout();
        mainSplit.setWidthFull();
        mainSplit.setSpacing(true);

        // Left Column: Roles List
        VerticalLayout rolesCol = new VerticalLayout();
        rolesCol.setWidth("35%");
        rolesCol.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.25rem");

        HorizontalLayout rolesHeader = new HorizontalLayout(new H3("Roles del Sistema"), new Button(VaadinIcon.PLUS.create()));
        rolesHeader.setWidthFull();
        rolesHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div role1 = createRoleItem("Administrador", "Acceso total al sistema.", false);
        Div role2 = createRoleItem("Médico", "Gestión de consultas y pacientes propios.", false);
        Div role3 = createRoleItem("Recepcionista", "Gestión de citas y agenda general.", true);

        rolesCol.add(rolesHeader, role1, role2, role3);

        // Right Column: Role Editor
        VerticalLayout editorCol = new VerticalLayout();
        editorCol.setWidth("65%");
        editorCol.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        H3 editTitle = new H3("Editando Rol");
        editTitle.getStyle().set("margin", "0 0 1rem 0");

        TextField nameField = new TextField("Nombre del Rol");
        nameField.setValue("Recepcionista");
        nameField.setWidth("48%");

        TextField descField = new TextField("Descripción (Opcional)");
        descField.setValue("Gestión de citas y agenda general.");
        descField.setWidth("48%");

        HorizontalLayout formFields = new HorizontalLayout(nameField, descField);
        formFields.setWidthFull();

        // Permission Groups
        H4 dashTitle = new H4("Dashboard");
        HorizontalLayout dashPerms = new HorizontalLayout(new Checkbox("Ver", true));

        H4 citasTitle = new H4("Citas");
        HorizontalLayout citasPerms = new HorizontalLayout(
                new Checkbox("Ver", true),
                new Checkbox("Crear", true),
                new Checkbox("Editar", true),
                new Checkbox("Cancelar", false),
                new Checkbox("Reprogramar", false)
        );

        H4 pacientesTitle = new H4("Pacientes");
        HorizontalLayout pacPerms = new HorizontalLayout(
                new Checkbox("Ver", true),
                new Checkbox("Crear", true),
                new Checkbox("Editar", false),
                new Checkbox("Eliminar", false)
        );

        // Actions
        Button discardBtn = new Button("Descartar");
        Button saveBtn = new Button("Guardar Cambios");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout actions = new HorizontalLayout(discardBtn, saveBtn);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setWidthFull();

        editorCol.add(editTitle, formFields, new Hr(), dashTitle, dashPerms, citasTitle, citasPerms, pacientesTitle, pacPerms, new Hr(), actions);

        mainSplit.add(rolesCol, editorCol);
        add(title, subtitle, alertBanner, mainSplit);
    }

    private Div createRoleItem(String name, String desc, boolean selected) {
        Div item = new Div();
        item.setWidthFull();
        item.getStyle()
                .set("padding", "0.875rem 1rem")
                .set("border-radius", "8px")
                .set("margin-bottom", "0.5rem")
                .set("border", selected ? "1px solid #1565D8" : "1px solid #f1f5f9")
                .set("background-color", selected ? "#f0f6ff" : "#ffffff")
                .set("cursor", "pointer");

        H4 itemTitle = new H4(name);
        itemTitle.getStyle().set("margin", "0").set("color", selected ? "#1565D8" : "#1e293b").set("font-size", "0.9375rem");

        Paragraph itemDesc = new Paragraph(desc);
        itemDesc.getStyle().set("margin", "0").set("color", "#64748b").set("font-size", "0.8125rem");

        item.add(itemTitle, itemDesc);
        return item;
    }
}
