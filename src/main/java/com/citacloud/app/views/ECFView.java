package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "e-cf", layout = MainLayout.class)
@PageTitle("e-CF | CitaCloud")
@PermitAll
public class ECFView extends ModuloFase3View {
    public ECFView() { super("e-CF", "Administre los comprobantes fiscales electrónicos de la clínica."); }
}
