package com.citacloud.app.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Base visual para los módulos financieros y operativos de la Fase 3. */
public abstract class ModuloFase3View extends VerticalLayout {
    protected ModuloFase3View(String titulo, String descripcion) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(new H2(titulo), new Paragraph(descripcion));
    }
}
