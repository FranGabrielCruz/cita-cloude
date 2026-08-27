package com.citacloud.app.views;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Marco consistente para módulos clínicos que concentra el contexto del paciente.
 */
public abstract class ModuloClinicoView extends VerticalLayout {
    protected ModuloClinicoView(String titulo, String descripcion) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(new H2(titulo), new Paragraph(descripcion));
    }
}
