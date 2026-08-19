package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "inventario", layout = MainLayout.class)
@PageTitle("Inventario | CitaCloud")
@PermitAll
public class InventarioView extends ModuloFase3View {
    public InventarioView() { super("Inventario", "Consulte y controle las existencias de la clínica."); }
}
