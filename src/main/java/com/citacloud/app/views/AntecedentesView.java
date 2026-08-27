package com.citacloud.app.views;

import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

@Route(value = "antecedentes", layout = MainLayout.class)
@PageTitle("Antecedentes | CitaCloud")
@PermitAll
public class AntecedentesView extends ModuloClinicoView {
    public AntecedentesView() {
        super("Antecedentes", "Administre antecedentes personales, familiares, quirúrgicos y patológicos.");
    }
}
