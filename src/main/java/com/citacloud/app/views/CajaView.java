package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "caja", layout = MainLayout.class)
@PageTitle("Caja | CitaCloud")
@PermitAll
public class CajaView extends ModuloFase3View {
    public CajaView() { super("Caja", "Controle aperturas, cierres y movimientos de caja."); }
}
