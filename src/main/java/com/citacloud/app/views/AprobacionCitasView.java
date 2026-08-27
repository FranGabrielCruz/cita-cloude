package com.citacloud.app.views;

import com.citacloud.app.models.Cita;
import com.citacloud.app.security.*;
import com.citacloud.app.services.CitaService;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.util.*;

@Route(value = "aprobacion-citas", layout = MainLayout.class)
@PageTitle("Aprobación de citas | CitaCloud")
@PermitAll
public class AprobacionCitasView extends VerticalLayout {
    private final CitaService service;
    private final UUID empresa;
    private final Grid<Cita> grid = new Grid<>(Cita.class, false);

    public AprobacionCitasView(CitaService s) {
        service = s;
        TenantUserDetails u = AuthService.getAuthenticatedUser();
        empresa = u == null ? null : u.getEmpresaId();
        setSizeFull();
        setPadding(true);
        add(new H2("Aprobación de citas"), new Paragraph("Revise y confirme las solicitudes pendientes de la clínica."));
        grid.addColumn(c -> c.getPaciente().getNombreCompleto()).setHeader("PACIENTE");
        grid.addColumn(c -> c.getMedico().getNombreCompleto()).setHeader("MÉDICO");
        grid.addColumn(Cita::getFecha).setHeader("FECHA");
        grid.addColumn(Cita::getHoraInicio).setHeader("HORA");
        grid.addColumn(Cita::getEstado).setHeader("ESTADO");
        grid.addComponentColumn(c -> {
            Button a = new Button("Aprobar", e -> {
                try {
                    service.aprobar(empresa, c.getId());
                    cargar();
                    Notification.show("Cita aprobada correctamente.");
                } catch (Exception x) {
                    Notification.show(x.getMessage());
                }
            });
            return a;
        }).setHeader("ACCIONES");
        grid.setWidthFull();
        add(grid);
        cargar();
    }

    private void cargar() {
        grid.setItems(empresa == null ? List.of() : service.listarPorEmpresa(empresa).stream().filter(c -> "PENDIENTE".equals(c.getEstado())).toList());
    }
}
