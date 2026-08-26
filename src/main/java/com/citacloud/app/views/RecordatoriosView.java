package com.citacloud.app.views;

import com.citacloud.app.models.Notificacion;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.NotificacionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Route(value = "recordatorios", layout = MainLayout.class)
@PageTitle("Notificaciones | CitaCloud")
@PermitAll
public class RecordatoriosView extends VerticalLayout {
    private final NotificacionService servicio;
    private final UUID empresaId;
    private final UUID usuarioId;
    private final VerticalLayout lista = new VerticalLayout();
    private final TextField buscar = new TextField();
    private String filtro = "TODAS";

    public RecordatoriosView(NotificacionService servicio) {
        this.servicio = servicio;
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        empresaId = usuario == null ? null : usuario.getEmpresaId();
        usuarioId = usuario == null ? null : usuario.getUsuarioId();
        setSizeFull();
        setPadding(true);
        // El pie es fijo; la reserva y el desplazamiento evitan que cubra las últimas tarjetas.
        getStyle().set("max-width", "1180px").set("margin", "0 auto")
                .set("padding-bottom", "13rem").set("box-sizing", "border-box")
                .set("overflow-y", "auto");

        Button leerTodas = new Button("Marcar todas como leídas", e -> {
            if (empresaId != null && usuarioId != null) servicio.leerTodas(empresaId, usuarioId);
            cargar();
        });
        leerTodas.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        H2 titulo = new H2("Notificaciones");
        HorizontalLayout encabezado = new HorizontalLayout(titulo, leerTodas);
        encabezado.setWidthFull();
        encabezado.setAlignItems(Alignment.CENTER);
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(encabezado, new Paragraph("Mantente al día con las actividades que requieren tu atención."), indicadores(), filtros());

        lista.setPadding(false);
        lista.setSpacing(false);
        lista.setWidthFull();
        add(lista);
        cargar();
    }

    private Component indicadores() {
        long sinLeer = datos().stream().filter(n -> !n.isLeida()).count();
        long citas = datos().stream().filter(n -> "CITAS".equals(n.getCategoria())).count();
        long importantes = datos().stream().filter(n -> "ALTA".equals(n.getPrioridad())).count();
        HorizontalLayout tarjetas = new HorizontalLayout(card("🔔 Sin leer", sinLeer), card("📅 Citas", citas),
                card("🧪 Resultados", 0), card("⚠ Importantes", importantes));
        tarjetas.setWidthFull();
        tarjetas.getStyle().set("flex-wrap", "wrap");
        return tarjetas;
    }

    private Component filtros() {
        HorizontalLayout tipos = new HorizontalLayout();
        tipos.setSpacing(true);
        tipos.getStyle().set("flex-shrink", "0");
        for (String etiqueta : List.of("Todas", "Sin leer", "Citas", "Clínicas", "Sistema")) {
            Button boton = new Button(etiqueta, e -> {
                filtro = etiqueta.toUpperCase().replace(" ", "_").replace("Í", "I");
                cargar();
            });
            boton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            tipos.add(boton);
        }
        buscar.setPlaceholder("Buscar notificaciones...");
        buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true);
        buscar.setWidth("auto");
        buscar.getStyle().set("min-width", "18rem");
        buscar.addValueChangeListener(e -> cargar());
        HorizontalLayout controles = new HorizontalLayout(tipos, buscar);
        controles.setWidthFull();
        controles.setAlignItems(Alignment.CENTER);
        controles.setFlexGrow(1, buscar);
        controles.getStyle().set("gap", "1.25rem").set("flex-wrap", "nowrap");
        return controles;
    }

    private void cargar() {
        lista.removeAll();
        List<Notificacion> visibles = datos().stream().filter(this::coincide).toList();
        if (visibles.isEmpty()) {
            VerticalLayout vacio = new VerticalLayout(new H2("🔔"), new H3("No tienes notificaciones."),
                    new Span("Cuando haya algo que requiera tu atención, aparecerá aquí."));
            vacio.setAlignItems(Alignment.CENTER);
            lista.add(vacio);
            return;
        }
        grupo("HOY", visibles.stream().filter(n -> n.getCreadaEn() != null
                && n.getCreadaEn().toLocalDate().equals(LocalDate.now())).toList());
        grupo("ANTERIORES", visibles.stream().filter(n -> n.getCreadaEn() == null
                || !n.getCreadaEn().toLocalDate().equals(LocalDate.now())).toList());
    }

    private void grupo(String titulo, List<Notificacion> notificaciones) {
        if (notificaciones.isEmpty()) return;
        lista.add(new H4(titulo));
        for (Notificacion notificacion : notificaciones) {
            Div tarjeta = new Div();
            tarjeta.getStyle().set("padding", "1rem").set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                    .set("background", notificacion.isLeida() ? "transparent" : "var(--lumo-primary-color-10pct)")
                    .set("border-radius", "8px");
            String hora = notificacion.getCreadaEn() == null ? "" : notificacion.getCreadaEn()
                    .format(DateTimeFormatter.ofPattern("h:mm a"));
            Button abrir = new Button("PAYMENT".equals(notificacion.getEntidadTipo()) ? "Ver pago" : "Ver detalle",
                    e -> abrir(notificacion));
            abrir.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            tarjeta.add(new Span((notificacion.isLeida() ? "○ " : "● ") + "🔔 "
                    + notificacion.getTitulo() + " · " + hora), new Paragraph(notificacion.getMensaje()), abrir);
            lista.add(tarjeta);
        }
    }

    private void abrir(Notificacion notificacion) {
        if (empresaId != null && usuarioId != null) servicio.leer(empresaId, usuarioId, notificacion.getId());
        getUI().ifPresent(ui -> ui.navigate("CITA".equals(notificacion.getEntidadTipo()) ? "citas"
                : "PAYMENT".equals(notificacion.getEntidadTipo()) ? "pagos?pago=" + notificacion.getEntidadId()
                : "recordatorios"));
    }

    private List<Notificacion> datos() {
        return empresaId == null || usuarioId == null ? List.of() : servicio.listar(empresaId, usuarioId);
    }

    private boolean coincide(Notificacion notificacion) {
        if ("SIN_LEER".equals(filtro) && notificacion.isLeida()) return false;
        if ("CITAS".equals(filtro) && !"CITAS".equals(notificacion.getCategoria())) return false;
        if ("CLINICAS".equals(filtro) && !"CLINICAS".equals(notificacion.getCategoria())) return false;
        if ("SISTEMA".equals(filtro) && !"SISTEMA".equals(notificacion.getCategoria())) return false;
        String consulta = buscar.getValue().toLowerCase();
        return consulta.isBlank() || notificacion.getTitulo().toLowerCase().contains(consulta)
                || notificacion.getMensaje().toLowerCase().contains(consulta);
    }

    private Component card(String titulo, long cantidad) {
        VerticalLayout tarjeta = new VerticalLayout(new Span(titulo), new H2(String.valueOf(cantidad)));
        tarjeta.setPadding(true);
        tarjeta.setSpacing(false);
        tarjeta.getStyle().set("min-width", "170px").set("flex", "1")
                .set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "12px");
        return tarjeta;
    }
}
