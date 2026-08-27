package com.citacloud.app.views;

import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

@Route(value = "ordenes-estudios", layout = MainLayout.class)
@PageTitle("Órdenes y estudios | CitaCloud")
@PermitAll
public class OrdenesEstudiosView extends ModuloClinicoView {
    public OrdenesEstudiosView() {
        super("Órdenes y estudios", "Solicite laboratorios, imágenes, procedimientos y otros estudios.");
    }
}
