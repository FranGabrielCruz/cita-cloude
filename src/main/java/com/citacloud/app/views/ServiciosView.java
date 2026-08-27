package com.citacloud.app.views;

import com.citacloud.app.models.Servicio;
import com.citacloud.app.security.*;
import com.citacloud.app.services.ServicioService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Route(value = "servicios", layout = MainLayout.class)
@PageTitle("Servicios | CitaCloud")
@PermitAll
public class ServiciosView extends VerticalLayout {
    final ServicioService service;
    final UUID empresa;
    final Grid<Servicio> grid = new Grid<>(Servicio.class, false);
    final PaginadorTabla<Servicio> pager = new PaginadorTabla<>(grid);
    final TextField search = new TextField();
    final ComboBox<String> state = new ComboBox<>();

    public ServiciosView(ServicioService s) {
        service = s;
        TenantUserDetails u = AuthService.getAuthenticatedUser();
        empresa = u == null ? null : u.getEmpresaId();
        setWidthFull();
        setPadding(true);
        pager.setFilasPorPagina(10);
        Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> form(null));
        nuevo.setTooltipText("Nuevo servicio");
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.getStyle().set("background", "#2563eb").set("color", "white");
        HorizontalLayout head = new HorizontalLayout(new H2("SERVICIOS"), nuevo);
        head.setWidthFull();
        head.setJustifyContentMode(JustifyContentMode.BETWEEN);
        search.setLabel("Buscar por código o servicio");
        search.setWidthFull();
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> load());
        state.setLabel("Estado");
        state.setItems("Todos", "Activos", "Inactivos");
        state.setValue("Todos");
        state.setWidthFull();
        state.addValueChangeListener(e -> load());
        HorizontalLayout filters = new HorizontalLayout(search, state);
        filters.setWidthFull();
        filters.setFlexGrow(2, search);
        filters.setFlexGrow(1, state);
        add(head, new Paragraph("Administra los servicios y procedimientos ofrecidos por la clínica."), filters);
        grid.addColumn(Servicio::getCodigo).setHeader("CÓDIGO");
        grid.addColumn(Servicio::getNombre).setHeader("SERVICIO").setFlexGrow(1);
        grid.addColumn(x -> money(x.getPrecio())).setHeader("PRECIO");
        grid.addColumn(x -> Boolean.TRUE.equals(x.getActivo()) ? "ACTIVO" : "INACTIVO").setHeader("ESTADO");
        grid.addComponentColumn(x -> {
            Button b = new Button(VaadinIcon.EDIT.create(), e -> form(x));
            b.setTooltipText("Editar");
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            b.getStyle().set("background", "transparent").set("border", "none").set("box-shadow", "none").set("padding", "0");
            return b;
        }).setHeader("ACCIONES");
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        add(grid, pager);
        load();
    }

    void load() {
        String q = search.getValue() == null ? "" : search.getValue().toLowerCase();
        pager.setItems(empresa == null ? List.of() : service.listar(empresa).stream().filter(x -> q.isBlank() || x.getCodigo().toLowerCase().contains(q) || x.getNombre().toLowerCase().contains(q)).filter(x -> "Todos".equals(state.getValue()) || ("Activos".equals(state.getValue()) == Boolean.TRUE.equals(x.getActivo()))).toList());
    }

    void form(Servicio x) {
        Dialog d = new Dialog();
        d.setHeaderTitle(x == null ? "Nuevo servicio" : "Editar servicio");
        d.setWidth("min(720px,96vw)");
        TextField name = new TextField("Nombre del servicio *", x == null ? "" : x.getNombre());
        TextField price = new TextField("Precio *");
        price.setPrefixComponent(new Span("RD$"));
        price.setPlaceholder("0.00");
        if (x != null && x.getPrecio() != null) price.setValue(formatearPrecio(x.getPrecio()));
        price.addBlurListener(event -> formatearPrecioEnCampo(price));
        TextArea desc = new TextArea("Descripción", x == null ? "" : x.getDescripcion());
        Checkbox active = new Checkbox("Activo", x == null || Boolean.TRUE.equals(x.getActivo()));
        FormLayout fields = new FormLayout(name, price, desc, active);
        fields.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("560px", 2));
        fields.setColspan(desc, 2);
        Button save = new Button(VaadinIcon.DISC.create(), e -> {
            try {
                service.guardar(empresa, x == null ? null : x.getId(), null, name.getValue(), desc.getValue(), obtenerPrecio(price.getValue()), active.getValue());
                d.close();
                load();
                Notification.show("Servicio guardado correctamente.");
            } catch (Exception z) {
                Notification.show(z.getMessage());
            }
        });
        save.setTooltipText("Guardar servicio");
        save.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.getStyle().set("background", "#16a34a").set("color", "white");
        Button close = new Button(VaadinIcon.CLOSE.create(), e -> d.close());
        close.setTooltipText("Cancelar");
        close.getStyle().set("background", "#e2e8f0").set("color", "black");
        d.add(fields);
        d.getFooter().add(save, close);
        d.open();
    }

    private void formatearPrecioEnCampo(TextField campo) {
        String valor = campo.getValue();
        if (valor == null || valor.isBlank()) return;
        try {
            campo.setValue(formatearPrecio(obtenerPrecio(valor)));
        } catch (NumberFormatException ignored) {
            // La validación del monto se muestra al guardar el formulario.
        }
    }

    private BigDecimal obtenerPrecio(String valor) {
        return new BigDecimal(valor.replace(",", "").trim());
    }

    private String formatearPrecio(BigDecimal valor) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)).format(valor);
    }

    String money(BigDecimal v) {
        return "RD$ " + (v == null ? BigDecimal.ZERO : v).setScale(2);
    }
}
