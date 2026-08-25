package com.citacloud.app.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Paginación reutilizable para tablas cargadas en memoria. */
public class PaginadorTabla<T> extends HorizontalLayout {

    private final Grid<T> tabla;
    private final ComboBox<Integer> filasPorPagina = new ComboBox<>();
    private final Span resumen = new Span();
    private final Button anterior = new Button(VaadinIcon.ANGLE_LEFT.create());
    private final Button siguiente = new Button(VaadinIcon.ANGLE_RIGHT.create());
    private List<T> elementos = List.of();
    private int paginaActual;

    public PaginadorTabla(Grid<T> tabla) {
        this.tabla = tabla;
        addClassName("paginador-tabla");
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        getStyle().set("margin-top", "0.35rem").set("padding", "0.65rem 0.85rem")
                .set("background", "#ffffff").set("border", "1px solid #e2e8f0")
                .set("border-radius", "10px").set("box-sizing", "border-box")
                .set("flex-wrap", "wrap").set("gap", "0.75rem");

        filasPorPagina.setAriaLabel("Filas por página");
        filasPorPagina.setItems(10, 20, 25, 50, 100);
        filasPorPagina.setValue(10);
        filasPorPagina.setWidth("80px");
        filasPorPagina.setAllowCustomValue(false);
        filasPorPagina.addValueChangeListener(event -> {
            paginaActual = 0;
            actualizarTabla();
        });

        anterior.setTooltipText("Página anterior");
        siguiente.setTooltipText("Página siguiente");
        anterior.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        siguiente.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        anterior.getStyle().set("border-radius", "8px").set("min-width", "2.25rem");
        siguiente.getStyle().set("border-radius", "8px").set("min-width", "2.25rem");
        anterior.addClickListener(event -> cambiarPagina(-1));
        siguiente.addClickListener(event -> cambiarPagina(1));
        resumen.getStyle().set("font-size", "0.82rem").set("color", "#64748b")
                .set("font-weight", "500").set("padding", "0 0.4rem");
        HorizontalLayout navegacion = new HorizontalLayout(resumen, anterior, siguiente);
        navegacion.setSpacing(false);
        navegacion.setAlignItems(FlexComponent.Alignment.CENTER);
        Span etiquetaFilas = new Span("Ver");
        Span sufijoFilas = new Span("por página");
        etiquetaFilas.getStyle().set("font-size", "0.82rem").set("color", "#64748b");
        sufijoFilas.getStyle().set("font-size", "0.82rem").set("color", "#64748b");
        HorizontalLayout configuracion = new HorizontalLayout(etiquetaFilas, filasPorPagina, sufijoFilas);
        configuracion.setSpacing(false);
        configuracion.setAlignItems(FlexComponent.Alignment.CENTER);
        configuracion.getStyle().set("gap", "0.35rem");
        add(configuracion, navegacion);
        actualizarTabla();
    }

    public void setItems(Collection<T> nuevosElementos) {
        elementos = nuevosElementos == null ? List.of() : new ArrayList<>(nuevosElementos);
        paginaActual = 0;
        actualizarTabla();
    }

    /** Configura la cantidad inicial de filas sin afectar el resto de las tablas. */
    public void setFilasPorPagina(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de filas debe ser mayor que cero.");
        }
        filasPorPagina.setValue(cantidad);
    }

    private void cambiarPagina(int cambio) {
        int paginas = totalPaginas();
        paginaActual = Math.max(0, Math.min(paginaActual + cambio, paginas - 1));
        actualizarTabla();
    }

    private void actualizarTabla() {
        int porPagina = filasPorPagina.getValue() == null ? 10 : filasPorPagina.getValue();
        int total = elementos.size();
        int paginas = totalPaginas();
        paginaActual = Math.min(paginaActual, paginas - 1);
        int inicio = Math.min(paginaActual * porPagina, total);
        int fin = Math.min(inicio + porPagina, total);
        tabla.setItems(elementos.subList(inicio, fin));
        resumen.setText(total == 0 ? "Sin registros" : "Mostrando " + (inicio + 1) + "–" + fin + " de " + total);
        anterior.setEnabled(paginaActual > 0);
        siguiente.setEnabled(paginaActual < paginas - 1);
    }

    private int totalPaginas() {
        int porPagina = filasPorPagina.getValue() == null ? 10 : filasPorPagina.getValue();
        return Math.max(1, (int) Math.ceil((double) elementos.size() / porPagina));
    }
}
