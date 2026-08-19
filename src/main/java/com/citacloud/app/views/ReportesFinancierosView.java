package com.citacloud.app.views;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "reportes-financieros", layout = MainLayout.class)
@PageTitle("Reportes financieros | CitaCloud")
@PermitAll
public class ReportesFinancierosView extends ModuloFase3View {
    public ReportesFinancierosView() { super("Reportes financieros", "Visualice los indicadores financieros y operativos de la clínica."); }
}
