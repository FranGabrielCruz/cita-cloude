package com.citacloud.app.views;

import com.citacloud.app.models.Cita;
import com.citacloud.app.models.Medico;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.CitaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Route(value = "mi-agenda", layout = MainLayout.class)
@PageTitle("Mi agenda | CitaCloud")
@PermitAll
public class MiAgendaView extends VerticalLayout {
    private final CitaService citaService; private final MedicoRepository medicoRepository; private final UUID empresaId; private final UUID usuarioId;
    private final DatePicker fecha = new DatePicker("Fecha"); private final TextField paciente = new TextField("Paciente"); private final ComboBox<String> estado = new ComboBox<>("Estado"); private final Grid<Cita> grid = new Grid<>(Cita.class, false);
    public MiAgendaView(CitaService citaService, MedicoRepository medicoRepository) {
        this.citaService=citaService; this.medicoRepository=medicoRepository; TenantUserDetails usuario=AuthService.getAuthenticatedUser(); empresaId=usuario==null?null:usuario.getEmpresaId(); usuarioId=usuario==null?null:usuario.getUsuarioId();
        setWidthFull(); setPadding(true); setSpacing(true); fecha.setValue(LocalDate.now()); configurarTabla();
        H2 titulo=new H2("Mi agenda"); titulo.getStyle().set("margin","0").set("font-size","1.5rem").set("font-weight","800"); Button buscar=new Button(VaadinIcon.SEARCH.create(),e->actualizar()); buscar.setTooltipText("Buscar"); buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); HorizontalLayout encabezado=new HorizontalLayout(titulo,buscar); encabezado.setWidthFull(); encabezado.setAlignItems(FlexComponent.Alignment.CENTER); encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        paciente.setPlaceholder("Buscar por nombre"); paciente.setClearButtonVisible(true); paciente.setWidthFull(); estado.setItems("Todos", "PENDIENTE", "CONFIRMADA", "EN_ESPERA", "EN_CONSULTA", "ATENDIDA", "CANCELADA"); estado.setValue("Todos"); estado.setItemLabelGenerator(this::etiqueta); fecha.addValueChangeListener(e->actualizar()); paciente.addValueChangeListener(e->actualizar()); estado.addValueChangeListener(e->actualizar()); HorizontalLayout filtro=new HorizontalLayout(fecha,paciente,estado); filtro.setWidthFull(); filtro.setFlexGrow(1,paciente); filtro.getStyle().set("background","#fff").set("padding","1rem").set("border-radius","12px").set("border","1px solid #e2e8f0");
        add(encabezado,filtro,grid); actualizar();
    }
    private void configurarTabla() { grid.addColumn(c->c.getHoraInicio()+" - "+c.getHoraFin()).setHeader("HORARIO").setWidth("150px").setFlexGrow(0); grid.addColumn(c->c.getPaciente().getNombreCompleto()).setHeader("PACIENTE").setWidth("250px").setFlexGrow(0); grid.addColumn(Cita::getMotivo).setHeader("MOTIVO").setFlexGrow(1); grid.addComponentColumn(c->{Span estado=new Span(etiqueta(c.getEstado())); estado.addClassName("CANCELADA".equals(c.getEstado())?"badge-cancelada":"EN_ESPERA".equals(c.getEstado())?"badge-pendiente":"badge-confirmada");return estado;}).setHeader("ESTADO").setWidth("150px").setFlexGrow(0); grid.addComponentColumn(this::acciones).setHeader("ACCIONES").setWidth("115px").setFlexGrow(0); grid.setWidthFull(); }
    private void actualizar() { if(empresaId==null||usuarioId==null){grid.setItems(List.of());return;} Medico medico=medicoRepository.findByEmpresaIdAndUsuarioId(empresaId,usuarioId).orElse(null); if(medico==null){grid.setItems(List.of()); Notification.show("Tu usuario no est\u00e1 asociado a un m\u00e9dico.",3000,Notification.Position.MIDDLE);return;} String nombre=paciente.getValue()==null?"":paciente.getValue().trim().toLowerCase(java.util.Locale.ROOT); grid.setItems(citaService.listarPorFecha(empresaId,fecha.getValue()).stream().filter(c->medico.getId().equals(c.getMedico().getId())).filter(c->nombre.isBlank() || c.getPaciente().getNombreCompleto().toLowerCase(java.util.Locale.ROOT).contains(nombre)).filter(c->"Todos".equals(estado.getValue()) || estado.getValue().equals(c.getEstado())).sorted(Comparator.comparing(Cita::getHoraInicio)).toList()); }
    private Button acciones(Cita cita) { String siguiente=("CONFIRMADA".equals(cita.getEstado()) || "EN_ESPERA".equals(cita.getEstado()))?"EN_CONSULTA":"EN_CONSULTA".equals(cita.getEstado())?"ATENDIDA":null; if(siguiente==null) { Button sinAccion=new Button("—"); sinAccion.setTooltipText("Sin acciones disponibles"); sinAccion.setEnabled(false); return sinAccion; } String texto="EN_CONSULTA".equals(siguiente)?"Iniciar":"Atender"; String ayuda="EN_CONSULTA".equals(siguiente)?"Iniciar consulta":"Marcar como atendida"; Button boton=new Button(texto,e->{try{citaService.cambiarEstadoClinico(empresaId,cita.getId(),usuarioId,false,true,false,siguiente);actualizar();Notification.show("Estado actualizado.",3000,Notification.Position.BOTTOM_START);}catch(IllegalArgumentException ex){Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE);}}); boton.setTooltipText(ayuda); boton.getStyle().set("background","EN_CONSULTA".equals(siguiente)?"#2563eb":"#16a34a").set("color","white").set("padding","0.45rem 0.65rem"); return boton; }
    private String etiqueta(String estado){return switch(estado){case "Todos"->"Todos";case "PENDIENTE"->"Pendiente";case "EN_ESPERA"->"En espera";case "EN_CONSULTA"->"En consulta";case "ATENDIDA"->"Atendida";case "CONFIRMADA"->"Confirmada";case "CANCELADA"->"Cancelada";default->estado;};}
}
