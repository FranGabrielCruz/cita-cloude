package com.citacloud.app.views;

import com.citacloud.app.models.Notificacion;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.NotificacionService;
import com.citacloud.app.views.components.PaginadorTabla;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "recordatorios", layout = MainLayout.class)
@PageTitle("Notificaciones | CitaCloud")
@PermitAll
public class RecordatoriosView extends VerticalLayout {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy · h:mm a", Locale.US);
    private final NotificacionService servicio;
    private final UUID empresaId;
    private final UUID usuarioId;
    private final Grid<Notificacion> tabla = new Grid<>(Notificacion.class, false);
    private final PaginadorTabla<Notificacion> paginador = new PaginadorTabla<>(tabla);
    private final HorizontalLayout indicadores = new HorizontalLayout();
    private final VerticalLayout vacio = new VerticalLayout();
    private final TextField buscar = new TextField();
    private String filtro = "TODAS";

    public RecordatoriosView(NotificacionService servicio) {
        this.servicio = servicio;
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        empresaId = usuario == null ? null : usuario.getEmpresaId();
        usuarioId = usuario == null ? null : usuario.getUsuarioId();

        setWidthFull(); setPadding(true); setSpacing(true);
        getStyle().set("max-width", "1280px").set("margin", "0 auto")
                .set("height", "auto").set("box-sizing", "border-box");

        Button leerTodas = new Button("Marcar todas como leídas", e -> {
            if (empresaId != null && usuarioId != null) servicio.leerTodas(empresaId, usuarioId);
            cargar();
        });
        leerTodas.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        H2 titulo = new H2("Notificaciones"); titulo.getStyle().set("margin", "0");
        HorizontalLayout encabezado = new HorizontalLayout(titulo, leerTodas);
        encabezado.setWidthFull(); encabezado.setAlignItems(Alignment.CENTER);
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);

        indicadores.setWidthFull(); indicadores.getStyle().set("flex-wrap", "wrap");
        configurarTabla(); configurarVacio();
        paginador.setFilasPorPagina(10);
        add(encabezado, new Paragraph("Mantente al día con las actividades que requieren tu atención."),
                indicadores, filtros(), tabla, paginador, vacio);
        cargar();
    }

    private void configurarTabla() {
        tabla.addComponentColumn(this::estado).setHeader("ESTADO").setAutoWidth(true).setFlexGrow(0);
        tabla.addColumn(n -> n.getCreadaEn() == null ? "-" : n.getCreadaEn().format(FECHA_HORA))
                .setHeader("FECHA").setAutoWidth(true).setSortable(true);
        tabla.addColumn(n -> categoria(n.getCategoria())).setHeader("CATEGORÍA").setAutoWidth(true);
        tabla.addComponentColumn(n -> {
            Span titulo = new Span(texto(n.getTitulo()));
            if (!n.isLeida()) titulo.getStyle().set("font-weight", "700");
            return titulo;
        }).setHeader("NOTIFICACIÓN").setWidth("220px").setFlexGrow(1);
        tabla.addColumn(n -> texto(n.getMensaje())).setHeader("MENSAJE").setWidth("320px").setFlexGrow(2);
        tabla.addColumn(n -> etiqueta(n.getPrioridad())).setHeader("PRIORIDAD").setAutoWidth(true);
        tabla.addComponentColumn(n -> {
            Button abrir = new Button(VaadinIcon.EYE.create(), e -> abrir(n));
            abrir.setTooltipText("PAYMENT".equals(n.getEntidadTipo()) ? "Ver pago" : "Ver detalle");
            abrir.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return abrir;
        }).setHeader("ACCIÓN").setAutoWidth(true).setFlexGrow(0);
        tabla.setWidthFull(); tabla.setAllRowsVisible(true);
        tabla.getStyle().set("flex", "0 0 auto");
        tabla.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)").set("overflow", "hidden");
    }

    private void configurarVacio() {
        vacio.add(new H2("🔔"), new H3("No tienes notificaciones."),
                new Span("Cuando haya algo que requiera tu atención, aparecerá aquí."));
        vacio.setAlignItems(Alignment.CENTER); vacio.setPadding(true); vacio.setVisible(false);
    }

    private Component filtros() {
        HorizontalLayout tipos = new HorizontalLayout(); tipos.setSpacing(true);
        tipos.getStyle().set("flex-shrink", "0");
        for (String etiqueta : List.of("Todas", "Sin leer", "Citas", "Clínicas", "Sistema")) {
            Button boton = new Button(etiqueta, e -> {
                filtro = etiqueta.toUpperCase(Locale.ROOT).replace(" ", "_").replace("Í", "I");
                cargar();
            });
            boton.addThemeVariants(ButtonVariant.LUMO_TERTIARY); tipos.add(boton);
        }
        buscar.setPlaceholder("Buscar notificaciones..."); buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true); buscar.setWidthFull(); buscar.setValueChangeMode(ValueChangeMode.LAZY);
        buscar.setValueChangeTimeout(300); buscar.getStyle().set("min-width", "18rem");
        buscar.addValueChangeListener(e -> cargar());
        HorizontalLayout controles = new HorizontalLayout(tipos, buscar); controles.setWidthFull();
        controles.setAlignItems(Alignment.CENTER); controles.setFlexGrow(1, buscar);
        controles.getStyle().set("gap", "1.25rem").set("flex-wrap", "wrap");
        return controles;
    }

    private void cargar() {
        List<Notificacion> todos = datos();
        List<Notificacion> visibles = todos.stream().filter(this::coincide).toList();
        paginador.setItems(visibles);
        tabla.setVisible(!visibles.isEmpty());
        paginador.setVisible(!visibles.isEmpty());
        vacio.setVisible(visibles.isEmpty());
        pintarIndicadores(todos);
    }

    private void pintarIndicadores(List<Notificacion> datos) {
        long sinLeer = datos.stream().filter(n -> !n.isLeida()).count();
        long citas = datos.stream().filter(n -> "CITAS".equals(n.getCategoria())).count();
        long resultados = datos.stream().filter(n -> Set.of("LABORATORIO", "RESULTADOS").contains(n.getCategoria())).count();
        long importantes = datos.stream().filter(n -> "ALTA".equals(n.getPrioridad())).count();
        indicadores.removeAll(); indicadores.add(card("🔔 Sin leer", sinLeer), card("📅 Citas", citas),
                card("🧪 Resultados", resultados), card("⚠ Importantes", importantes));
    }

    private Component estado(Notificacion notificacion) {
        Span valor = new Span(notificacion.isLeida() ? "○ Leída" : "● Sin leer");
        valor.getStyle().set("color", notificacion.isLeida() ? "var(--lumo-secondary-text-color)" : "var(--lumo-primary-color)")
                .set("font-weight", notificacion.isLeida() ? "500" : "700");
        return valor;
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
        String consulta = buscar.getValue().trim().toLowerCase(Locale.ROOT);
        return consulta.isBlank() || texto(notificacion.getTitulo()).toLowerCase(Locale.ROOT).contains(consulta)
                || texto(notificacion.getMensaje()).toLowerCase(Locale.ROOT).contains(consulta)
                || texto(notificacion.getCategoria()).toLowerCase(Locale.ROOT).contains(consulta);
    }

    private Component card(String titulo, long cantidad) {
        VerticalLayout tarjeta = new VerticalLayout(new Span(titulo), new H2(String.valueOf(cantidad)));
        tarjeta.setPadding(true); tarjeta.setSpacing(false);
        tarjeta.getStyle().set("min-width", "170px").set("flex", "1")
                .set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "12px");
        return tarjeta;
    }

    private String categoria(String valor) {
        return switch (texto(valor)) {
            case "CITAS" -> "Citas"; case "CLINICAS" -> "Clínicas"; case "SISTEMA" -> "Sistema";
            case "PAGOS" -> "Pagos"; case "CAJA" -> "Caja"; case "LABORATORIO", "RESULTADOS" -> "Resultados";
            default -> etiqueta(valor);
        };
    }

    private String etiqueta(String valor) {
        if (valor == null || valor.isBlank()) return "-";
        String limpio = valor.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(limpio.charAt(0)) + limpio.substring(1);
    }

    private String texto(String valor) { return valor == null ? "" : valor; }
}
