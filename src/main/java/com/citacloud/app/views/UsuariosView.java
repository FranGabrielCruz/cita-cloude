package com.citacloud.app.views;

import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.UsuarioRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
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

@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | CitaCloud")
@PermitAll
public class UsuariosView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;

    public UsuariosView(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        H2 title = new H2("Usuarios y Accesos");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");

        Button nuevoUsuarioBtn = new Button("Nuevo Usuario", VaadinIcon.PLUS.create());
        nuevoUsuarioBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoUsuarioBtn.getStyle().set("background-color", "#1565D8");

        HorizontalLayout headerRow = new HorizontalLayout(title, nuevoUsuarioBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Grid<Usuario> grid = new Grid<>(Usuario.class, false);
        grid.addColumn(Usuario::getUsuario).setHeader("USUARIO");
        grid.addColumn(Usuario::getNombreCompleto).setHeader("NOMBRE");
        grid.addColumn(Usuario::getEmail).setHeader("EMAIL");
        grid.addColumn(Usuario::getTelefono).setHeader("TELÉFONO");
        grid.addComponentColumn(u -> {
            Span chip = new Span(Boolean.TRUE.equals(u.getActivo()) ? "Activo" : "Inactivo");
            chip.addClassName(Boolean.TRUE.equals(u.getActivo()) ? "badge-activo" : "badge-inactivo");
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(usuarioRepository.findByEmpresaId(empresaId));
        }

        add(headerRow, grid);
    }
}
