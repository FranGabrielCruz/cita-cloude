package com.citacloud.app.views;

import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

@Route(value = "tratamientos", layout = MainLayout.class)
@PageTitle("Tratamientos | CitaCloud")
@PermitAll
public class TratamientosView extends ModuloClinicoView {
    public TratamientosView() {
        super("Tratamientos", "Gestione los tratamientos e indicaciones asociados al paciente.");
    }
}
