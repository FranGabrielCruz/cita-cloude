package com.citacloud.app.views;

import com.citacloud.app.models.Cita;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.DashboardService;
import com.citacloud.app.services.SucursalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import java.util.List;
import java.util.UUID;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | CitaCloud")
@PermitAll
@CssImport("./styles/dashboard-responsive.css")
public class DashboardView extends VerticalLayout {

    private final DashboardService dashboardService;
    private final SucursalService sucursalService;

    public DashboardView(DashboardService dashboardService, SucursalService sucursalService) {
        this.dashboardService = dashboardService;
        this.sucursalService = sucursalService;

        // El dashboard debe crecer con sus tarjetas y tablas para que el scroll
        // de la aplicaci\u00f3n deje visible el contenido antes del footer fijo.
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        UUID empresaId = user != null ? user.getEmpresaId() : null;

        // Header Section
        String nombreUsuario = user != null ? user.getNombreCompleto() : "Gabriel";
        H1 welcomeTitle = new H1("Hola, " + nombreUsuario);
        welcomeTitle.getStyle().set("font-size", "1.75rem").set("font-weight", "800").set("margin", "0");

        String nombreSucursal = empresaId == null ? "" : sucursalService.listarPorEmpresa(empresaId).stream()
                .findFirst().map(sucursal -> sucursal.getNombre()).orElse("");
        Span subTitle = new Span(nombreSucursal.isBlank() ? "Resumen del día" : nombreSucursal + " — Resumen del día");
        subTitle.getStyle().set("color", "#64748b").set("font-size", "0.9375rem");

        HorizontalLayout headerRow = new HorizontalLayout(new Div(welcomeTitle, subTitle));
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Metrics Grid (4 cards matching desing/dashboard.png)
        long confirmadas = empresaId != null ? dashboardService.getCitasHoyPorEstado(empresaId, "CONFIRMADA") : 0;
        long atendidas = empresaId != null ? dashboardService.getCitasHoyPorEstado(empresaId, "ATENDIDA") : 0;
        long canceladas = empresaId != null ? dashboardService.getCitasHoyPorEstado(empresaId, "CANCELADA") : 0;
        long medicos = empresaId != null ? dashboardService.getMedicosActivosCount(empresaId) : 0;

        var card1 = createMetricCard("CONFIRMADAS", String.valueOf(confirmadas), VaadinIcon.CHECK_CIRCLE, "#f0fdf4", "#16a34a");
        var card2 = createMetricCard("PACIENTES ATENDIDOS", String.valueOf(atendidas), VaadinIcon.USERS, "#f0fdfa", "#0d9488");
        var card3 = createMetricCard("CANCELADAS", String.valueOf(canceladas), VaadinIcon.CLOSE_CIRCLE, "#fef2f2", "#dc2626");
        var card4 = createMetricCard("MÉDICOS ACTIVOS", String.valueOf(medicos), VaadinIcon.DOCTOR, "#eff6ff", "#2563eb");

        HorizontalLayout metricsRow = new HorizontalLayout(card1, card2, card3, card4);
        metricsRow.addClassName("dashboard-metrics");
        metricsRow.setWidthFull();
        metricsRow.setFlexGrow(1, card1, card2, card3, card4);

        // Main 2-Column Section
        HorizontalLayout mainSection = new HorizontalLayout();
        mainSection.addClassName("dashboard-main");
        mainSection.setWidthFull();
        mainSection.setSpacing(true);

        // Left Column: Recent Appointments Grid
        VerticalLayout leftCol = new VerticalLayout();
        leftCol.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        H3 tableTitle = new H3("Citas Recientes");
        tableTitle.getStyle().set("margin", "0 0 1rem 0").set("font-size", "1.125rem");

        Grid<Cita> grid = new Grid<>(Cita.class, false);
        grid.addColumn(c -> c.getPaciente() != null ? c.getPaciente().getNombreCompleto() : "Paciente").setHeader("PACIENTE");
        grid.addColumn(c -> c.getMedico() != null ? c.getMedico().getNombreCompleto() + " (" + c.getMedico().getEspecialidadesTexto() + ")" : "Médico").setHeader("MÉDICOS / ESPECIALIDAD");
        grid.addComponentColumn(c -> {
            Span chip = new Span(c.getEstado());
            String estadoUpper = c.getEstado() != null ? c.getEstado().toUpperCase() : "PENDIENTE";
            if ("CONFIRMADA".equals(estadoUpper)) {
                chip.addClassName("badge-confirmada");
            } else if ("PENDIENTE".equals(estadoUpper)) {
                chip.addClassName("badge-pendiente");
            } else if ("CANCELADA".equals(estadoUpper)) {
                chip.addClassName("badge-cancelada");
            } else {
                chip.addClassName("badge-atendida");
            }
            return chip;
        }).setHeader("ESTADO");

        if (empresaId != null) {
            grid.setItems(dashboardService.getCitasRecientes(empresaId));
        }

        leftCol.add(tableTitle, grid);
        leftCol.addClassName("dashboard-left");
        leftCol.setWidth("65%");

        // Right Column: Breakdown & Agenda Timeline Cards
        VerticalLayout rightCol = new VerticalLayout();
        rightCol.addClassName("dashboard-right");
        rightCol.setWidth("35%");
        rightCol.setSpacing(true);

        // Card 1: Citas por Estado
        VerticalLayout chartCard = new VerticalLayout();
        chartCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        H3 chartTitle = new H3("Citas por estado de hoy");
        chartTitle.getStyle().set("margin", "0 0 1rem 0").set("font-size", "1.125rem");

        Div summaryBox = new Div();
        summaryBox.getStyle().set("text-align", "center").set("padding", "1rem").set("background-color", "#f8fafc").set("border-radius", "8px");
        DashboardService.ResumenEstadosHoy resumenHoy = empresaId == null
                ? new DashboardService.ResumenEstadosHoy(0, 0, 0, 0, 0, 0, 0, 0, 0)
                : dashboardService.getResumenEstadosHoy(empresaId);
        H2 totalNum = new H2(String.valueOf(resumenHoy.total()));
        totalNum.getStyle().set("margin", "0").set("color", "#1565D8");
        Span totalLabel = new Span("Total Citas");
        totalLabel.getStyle().set("font-size", "0.8125rem").set("color", "#64748b");
        summaryBox.add(totalNum, totalLabel);

        UnorderedList list = new UnorderedList();
        list.getStyle().set("list-style", "none").set("padding", "0").set("margin", "1rem 0 0 0");
        list.add(itemEstado("Pendientes", resumenHoy.pendientes(), resumenHoy.total(), "#f97316"));
        list.add(itemEstado("Confirmadas", resumenHoy.confirmadas(), resumenHoy.total(), "#22c55e"));
        list.add(itemEstado("En espera", resumenHoy.enEspera(), resumenHoy.total(), "#eab308"));
        list.add(itemEstado("En consulta", resumenHoy.enConsulta(), resumenHoy.total(), "#2563eb"));
        list.add(itemEstado("Atendidas", resumenHoy.atendidas(), resumenHoy.total(), "#0d9488"));
        list.add(itemEstado("Canceladas", resumenHoy.canceladas(), resumenHoy.total(), "#ef4444"));
        list.add(itemEstado("Reprogramadas", resumenHoy.reprogramadas(), resumenHoy.total(), "#8b5cf6"));
        list.add(itemEstado("No asistió", resumenHoy.noAsistio(), resumenHoy.total(), "#64748b"));

        chartCard.add(chartTitle, summaryBox, list);

        // Card 2: Agenda del Día Timeline
        VerticalLayout timelineCard = new VerticalLayout();
        timelineCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "1.5rem");

        H3 timelineTitle = new H3("Agenda del día");
        timelineTitle.getStyle().set("margin", "0 0 1rem 0").set("font-size", "1.125rem");

        Paragraph t1 = new Paragraph("🔵 08:00 AM - 12:00 PM: Turno Matutino (45 citas programadas)");
        t1.getStyle().set("font-size", "0.875rem").set("margin", "0 0 0.5rem 0");

        Paragraph t2 = new Paragraph("⚪ 12:00 PM - 02:00 PM: Receso / Emergencias");
        t2.getStyle().set("font-size", "0.875rem").set("margin", "0 0 0.5rem 0");

        Paragraph t3 = new Paragraph("🔵 02:00 PM - 06:00 PM: Turno Vespertino (62 citas programadas)");
        t3.getStyle().set("font-size", "0.875rem").set("margin", "0");

        timelineCard.add(timelineTitle);
        List<Cita> agendaHoy = empresaId == null ? List.of() : dashboardService.getAgendaHoy(empresaId);
        if (agendaHoy.isEmpty()) {
            Paragraph sinCitas = new Paragraph("No hay citas programadas para hoy.");
            sinCitas.getStyle().set("color", "#64748b").set("margin", "0");
            timelineCard.add(sinCitas);
        } else {
            agendaHoy.forEach(cita -> {
                String paciente = cita.getPaciente() == null ? "Paciente" : cita.getPaciente().getNombreCompleto();
                String medico = cita.getMedico() == null ? "Médico" : cita.getMedico().getNombreCompleto();
                Paragraph itemAgenda = new Paragraph("• " + cita.getHoraInicio() + " - " + cita.getHoraFin()
                        + ": " + paciente + " con " + medico);
                itemAgenda.getStyle().set("font-size", "0.875rem").set("margin", "0 0 0.5rem 0");
                timelineCard.add(itemAgenda);
            });
        }

        rightCol.add(chartCard, timelineCard);
        mainSection.add(leftCol, rightCol);

        add(headerRow, metricsRow, mainSection);
    }

    private VerticalLayout createMetricCard(String label, String value, VaadinIcon icon, String iconBg, String iconColor) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("metric-card");
        card.setSpacing(false);
        card.setPadding(false);

        Div iconDiv = new Div(icon.create());
        iconDiv.addClassName("metric-icon");
        iconDiv.getStyle().set("background-color", iconBg).set("color", iconColor);

        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");

        Div valueDiv = new Div(value);
        valueDiv.addClassName("metric-value");

        card.add(iconDiv, labelSpan, valueDiv);
        return card;
    }

    private String porcentaje(long cantidad, long total) {
        return total == 0 ? "0%" : Math.round(cantidad * 100.0 / total) + "%";
    }

    private ListItem itemEstado(String nombre, long cantidad, long total, String color) {
        Span punto = new Span();
        punto.getStyle().set("display", "inline-block").set("width", "0.75rem").set("height", "0.75rem")
                .set("margin-right", "0.5rem").set("border-radius", "50%").set("background-color", color);
        return new ListItem(punto, new Span(nombre + ": " + cantidad + " (" + porcentaje(cantidad, total) + ")"));
    }
}
