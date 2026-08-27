package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "ars", layout = MainLayout.class)
@PageTitle("ARS | CitaCloud")
@PermitAll
public class ARSView extends ModuloFase3View {
    public ARSView() {
        super("ARS", "Gestione las aseguradoras y sus procesos operativos.");
    }
}
