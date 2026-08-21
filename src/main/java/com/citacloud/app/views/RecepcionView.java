package com.citacloud.app.views;

import com.citacloud.app.models.Cita;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.CheckInService;
import com.citacloud.app.services.CitaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Route(value = "recepcion", layout = MainLayout.class)
@PageTitle("Recepción | CitaCloud")
@PermitAll
public class RecepcionView extends VerticalLayout {

    private final CitaService citas;
    private final CheckInService checkIns;
    private final TenantUserDetails usuario;
    private final UUID empresaId;
    private final Tab aprobacion = new Tab();
    private final Tab checkIn = new Tab();
    private final Tab salaEspera = new Tab();
    private final VerticalLayout contenido = new VerticalLayout();

    public RecepcionView(CitaService citas, CheckInService checkIns) {
        this.citas = citas;
        this.checkIns = checkIns;
        usuario = AuthService.getAuthenticatedUser();
        empresaId = usuario == null ? null : usuario.getEmpresaId();

        setSizeFull();
        setPadding(true);
        add(new H2("Recepción"), new Paragraph("Gestione la aprobación, llegada y espera de los pacientes."));

        Tabs pestañas = new Tabs(aprobacion, checkIn, salaEspera);
        pestañas.setWidthFull();
        pestañas.addSelectedChangeListener(evento -> mostrarPestana(evento.getSelectedTab()));
        contenido.setPadding(false);
        contenido.setSpacing(false);
        contenido.setWidthFull();
        add(pestañas, contenido);

        actualizarContadores();
        mostrarPestana(aprobacion);
    }

    private void mostrarPestana(Tab seleccionada) {
        contenido.removeAll();
        if (seleccionada == aprobacion) contenido.add(tablaAprobacion());
        else if (seleccionada == checkIn) contenido.add(tablaCheckIn());
        else contenido.add(tablaSalaEspera());
    }

    private Grid<Cita> tablaAprobacion() {
        Grid<Cita> tabla = tablaBase();
        tabla.addComponentColumn(cita -> new Button("Aprobar", evento -> {
            try {
                citas.aprobar(empresaId, cita.getId());
                Notification.show("Cita aprobada correctamente.");
                actualizarVistaActual();
            } catch (IllegalArgumentException error) {
                Notification.show(error.getMessage());
            }
        })).setHeader("ACCIONES");
        tabla.setItems(citasPendientes());
        return tabla;
    }

    private Grid<Cita> tablaCheckIn() {
        Grid<Cita> tabla = new Grid<>(Cita.class, false);
        tabla.addColumn(cita -> cita.getPaciente().getNombreCompleto()).setHeader("PACIENTE");
        tabla.addColumn(cita -> cita.getPaciente().getDocumento()).setHeader("DOCUMENTO");
        tabla.addColumn(cita -> cita.getMedico().getNombreCompleto()).setHeader("MÉDICO");
        tabla.addColumn(Cita::getHoraInicio).setHeader("HORA");
        tabla.addComponentColumn(cita -> new Button("Registrar llegada", evento -> {
            try {
                checkIns.registrar(empresaId, usuario.getUsuarioId(), cita.getId());
                Notification.show("Llegada registrada.");
                actualizarVistaActual();
            } catch (IllegalArgumentException error) {
                Notification.show(error.getMessage());
            }
        })).setHeader("ACCIÓN");
        tabla.setItems(citasConfirmadasHoy());
        tabla.setWidthFull();
        return tabla;
    }

    private Grid<Cita> tablaSalaEspera() {
        Grid<Cita> tabla = new Grid<>(Cita.class, false);
        tabla.addColumn(cita -> cita.getPaciente().getNombreCompleto()).setHeader("PACIENTE");
        tabla.addColumn(cita -> cita.getMedico().getNombreCompleto()).setHeader("MÉDICO");
        tabla.addColumn(Cita::getHoraInicio).setHeader("HORA");
        tabla.addColumn(cita -> cita.getConsultorio() == null ? "—" : cita.getConsultorio().getNombre()).setHeader("CONSULTORIO");
        tabla.addComponentColumn(cita -> new Button("Iniciar consulta", evento -> {
            try {
                citas.cambiarEstadoClinico(empresaId, cita.getId(), usuario.getUsuarioId(), true, false, false, "EN_CONSULTA");
                Notification.show("Consulta iniciada.");
                actualizarVistaActual();
            } catch (IllegalArgumentException error) {
                Notification.show(error.getMessage());
            }
        })).setHeader("ACCIÓN");
        tabla.setItems(citasEnEspera());
        tabla.setWidthFull();
        return tabla;
    }

    private Grid<Cita> tablaBase() {
        Grid<Cita> tabla = new Grid<>(Cita.class, false);
        tabla.addColumn(cita -> cita.getPaciente().getNombreCompleto()).setHeader("PACIENTE");
        tabla.addColumn(cita -> cita.getMedico().getNombreCompleto()).setHeader("MÉDICO");
        tabla.addColumn(Cita::getFecha).setHeader("FECHA");
        tabla.addColumn(Cita::getHoraInicio).setHeader("HORA");
        tabla.addColumn(Cita::getEstado).setHeader("ESTADO");
        tabla.setWidthFull();
        return tabla;
    }

    private void actualizarVistaActual() {
        actualizarContadores();
        mostrarPestana(aprobacion.isSelected() ? aprobacion : checkIn.isSelected() ? checkIn : salaEspera);
    }

    private void actualizarContadores() {
        aprobacion.setLabel("Aprobación (" + citasPendientes().size() + ")");
        checkIn.setLabel("Check-in (" + citasConfirmadasHoy().size() + ")");
        salaEspera.setLabel("Sala de espera (" + citasEnEspera().size() + ")");
    }

    private List<Cita> citasPendientes() {
        return empresaId == null ? List.of() : citas.listarPorEmpresa(empresaId).stream()
                .filter(cita -> "PENDIENTE".equals(cita.getEstado())).toList();
    }

    private List<Cita> citasConfirmadasHoy() {
        return empresaId == null ? List.of() : citas.listarPorFecha(empresaId, LocalDate.now()).stream()
                .filter(cita -> "CONFIRMADA".equals(cita.getEstado())).toList();
    }

    private List<Cita> citasEnEspera() {
        return empresaId == null ? List.of() : citas.listarPorEmpresa(empresaId).stream()
                .filter(cita -> "EN_ESPERA".equals(cita.getEstado())).toList();
    }
}
