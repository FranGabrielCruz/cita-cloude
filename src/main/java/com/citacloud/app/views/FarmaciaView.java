package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "farmacia", layout = MainLayout.class)
@PageTitle("Farmacia | CitaCloud")
@PermitAll
public class FarmaciaView extends ModuloFase3View {
    public FarmaciaView() { super("Farmacia", "Administre medicamentos y sus movimientos de dispensación."); }
}
