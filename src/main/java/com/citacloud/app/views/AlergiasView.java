package com.citacloud.app.views;

import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

@Route(value = "alergias", layout = MainLayout.class)
@PageTitle("Alergias | CitaCloud")
@PermitAll
public class AlergiasView extends ModuloClinicoView {
    public AlergiasView() {
        super("Alergias", "Mantenga alertas clínicas por alergeno y severidad.");
    }
}
