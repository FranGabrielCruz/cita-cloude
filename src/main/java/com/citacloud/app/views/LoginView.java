package com.citacloud.app.views;

import com.citacloud.app.security.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Iniciar Sesión | CitaCloud")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;

        setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, #f0f4ff 0%, #e6eefc 100%)")
                .set("box-sizing", "border-box")
                .set("overflow-y", "auto")
                .set("padding", "3.5rem 1.5rem 1.5rem");

        // Card Container
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setMaxWidth("420px");
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 12px 32px rgba(21, 101, 216, 0.08)")
                .set("border", "1px solid #e2e8f0");

        // Header Icon
        Div iconBox = new Div(VaadinIcon.HOSPITAL.create());
        iconBox.getStyle()
                .set("width", "56px")
                .set("height", "56px")
                .set("background-color", "#1565D8")
                .set("color", "#ffffff")
                .set("border-radius", "14px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 0.5rem auto")
                .set("font-size", "1.75rem");

        H2 title = new H2("CitaCloud");
        title.getStyle()
                .set("margin", "0")
                .set("text-align", "center")
                .set("color", "#1565D8")
                .set("font-weight", "800")
                .set("font-size", "1.75rem");

        Paragraph subtitle = new Paragraph("Gestión de citas avanzada");
        subtitle.getStyle()
                .set("margin", "0 0 0.75rem 0")
                .set("text-align", "center")
                .set("color", "#64748b")
                .set("font-size", "0.9375rem");

        // Inputs
        TextField empresaField = new TextField("Empresa");
        empresaField.setPlaceholder("Ej. CLINICA01");
        empresaField.setPrefixComponent(VaadinIcon.BUILDING.create());
        empresaField.setValue("CLINICA01");
        empresaField.setWidthFull();
        empresaField.getStyle().set("margin-bottom", "0.75rem");

        TextField usuarioField = new TextField("Usuario");
        usuarioField.setPlaceholder("Nombre de usuario");
        usuarioField.setPrefixComponent(VaadinIcon.USER.create());
        usuarioField.setValue("admin");
        usuarioField.setWidthFull();
        usuarioField.getStyle().set("margin-bottom", "0.75rem");

        PasswordField passwordField = new PasswordField("Contraseña");
        passwordField.setPlaceholder("••••••••");
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setValue("admin123");
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "0.75rem");

        // Options Row
        Checkbox rememberCheck = new Checkbox("Recordar sesión");


        HorizontalLayout optionsRow = new HorizontalLayout(rememberCheck);
        optionsRow.setWidthFull();
        optionsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        optionsRow.setAlignItems(FlexComponent.Alignment.CENTER);
        optionsRow.getStyle().set("margin-bottom", "0.75rem");

        // Submit Button
        Button loginButton = new Button("Iniciar sesión");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("background-color", "#1565D8")
                .set("border-radius", "8px")
                .set("height", "44px")
                .set("font-weight", "600")
                .set("font-size", "1rem");

        loginButton.addClickListener(e -> {
            boolean success = authService.login(
                    empresaField.getValue(),
                    usuarioField.getValue(),
                    passwordField.getValue()
            );

            if (success) {
                UI.getCurrent().navigate("");
            } else {
                Notification notification = Notification.show("Credenciales o Empresa inválidos", 3000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        card.add(iconBox, title, subtitle, empresaField, usuarioField, passwordField, optionsRow, loginButton);

        // Footer
        Paragraph footerText = new Paragraph("Soporte Técnico  •  Términos y Condiciones  •  Política de Privacidad");
        footerText.getStyle().set("color", "#64748b").set("font-size", "0.8125rem").set("margin", "1rem 0 0 0");

        Paragraph copyright = new Paragraph("© 2024 CitaCloud. Todos los derechos reservados.");
        copyright.getStyle().set("color", "#94a3b8").set("font-size", "0.75rem").set("margin", "0");

        add(card, footerText, copyright);
    }
}
