package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "laboratorio", layout = MainLayout.class)
@PageTitle("Laboratorio | CitaCloud")
@PermitAll
public class LaboratorioView extends ModuloFase3View {
    public LaboratorioView() { super("Laboratorio", "Organice los estudios y resultados de laboratorio."); }
}
