package com.citacloud.app.views;

import com.citacloud.app.models.*;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Route(value = "horarios", layout = MainLayout.class)
@PageTitle("Horarios | CitaCloud")
@PermitAll
public class HorariosView extends VerticalLayout {
    private final HorarioMedicoService horarioService; private final DescansoHorarioService descansoService; private final AusenciaMedicoService ausenciaService;
    private final MedicoService medicoService; private final SucursalService sucursalService; private final ConsultorioService consultorioService;
    private final UUID empresaId; private final Grid<HorarioMedico> grid = new Grid<>(HorarioMedico.class, false); private final Grid<AusenciaMedico> ausenciasGrid = new Grid<>(AusenciaMedico.class, false);
    private final ComboBox<Medico> medicoFiltro = new ComboBox<>("M\u00e9dico"); private final ComboBox<Sucursal> sucursalFiltro = new ComboBox<>("Sucursal");
    private final Div semanal = new Div();

    public HorariosView(HorarioMedicoService horarioService, DescansoHorarioService descansoService, AusenciaMedicoService ausenciaService, MedicoService medicoService, SucursalService sucursalService, ConsultorioService consultorioService) {
        this.horarioService = horarioService; this.descansoService = descansoService; this.ausenciaService = ausenciaService; this.medicoService = medicoService; this.sucursalService = sucursalService; this.consultorioService = consultorioService;
        TenantUserDetails user = AuthService.getAuthenticatedUser(); empresaId = user == null ? null : user.getEmpresaId();
        setWidthFull(); setPadding(true); setSpacing(true);
        getStyle().set("overflow-y", "visible").set("overflow-x", "hidden");
        configurarFiltros(); configurarTabla(); configurarAusencias(); add(encabezado(), filtros(), semanal, grid, new H2("Ausencias registradas"), ausenciasGrid); actualizar();
    }
    private void configurarFiltros() {
        medicoFiltro.setItemLabelGenerator(Medico::getNombreCompleto); sucursalFiltro.setItemLabelGenerator(Sucursal::getNombre);
        medicoFiltro.setPlaceholder("Seleccione m\u00e9dico"); sucursalFiltro.setPlaceholder("Seleccione sucursal"); medicoFiltro.setClearButtonVisible(true); sucursalFiltro.setClearButtonVisible(true);
        if (empresaId != null) { medicoFiltro.setItems(medicoService.listarActivos(empresaId)); sucursalFiltro.setItems(sucursalService.listarActivas(empresaId)); }
    }
    private HorizontalLayout encabezado() {
        H2 titulo = new H2("Horarios"); titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        Button nuevo = new Button(VaadinIcon.PLUS.create(), e -> formularioHorario()); nuevo.setTooltipText("Agregar horario"); nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY); nuevo.getStyle().set("background-color", "#16a34a").set("color", "white");
        Button ausencia = new Button(VaadinIcon.CALENDAR.create(), e -> formularioAusencia(null)); ausencia.setTooltipText("Registrar ausencia"); ausencia.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button buscar = new Button(VaadinIcon.SEARCH.create(), e -> actualizar()); buscar.setTooltipText("Buscar"); buscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button limpiar = new Button(VaadinIcon.ERASER.create(), e -> { medicoFiltro.clear(); sucursalFiltro.clear(); actualizar(); }); limpiar.setTooltipText("Limpiar"); limpiar.getStyle().set("background-color", "#e2e8f0").set("color", "#334155");
        HorizontalLayout acciones = new HorizontalLayout(nuevo, ausencia, buscar, limpiar); acciones.setSpacing(false); acciones.getStyle().set("gap", "0.35rem");
        HorizontalLayout row = new HorizontalLayout(titulo, acciones); row.setWidthFull(); row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); row.setAlignItems(FlexComponent.Alignment.CENTER); return row;
    }
    private HorizontalLayout filtros() { HorizontalLayout filtros = new HorizontalLayout(medicoFiltro, sucursalFiltro); filtros.setWidthFull(); filtros.setFlexGrow(1, medicoFiltro, sucursalFiltro); filtros.getStyle().set("background", "#fff").set("padding", "1rem").set("border-radius", "12px").set("border", "1px solid #e2e8f0"); return filtros; }
    private void configurarTabla() {
        grid.addColumn(h -> dia(h.getDiaSemana())).setHeader("D\u00cdA"); grid.addColumn(h -> h.getMedico().getNombreCompleto()).setHeader("M\u00c9DICO"); grid.addColumn(h -> h.getHoraInicio() + " - " + h.getHoraFin()).setHeader("HORARIO"); grid.addColumn(h -> h.getSucursal().getNombre()).setHeader("SUCURSAL"); grid.addColumn(h -> h.getConsultorio() == null ? "-" : h.getConsultorio().getNombre()).setHeader("CONSULTORIO"); grid.addColumn(h -> h.getDuracionCitaMinutos() + " min").setHeader("DURACI\u00d3N");
        grid.addComponentColumn(h -> { Span estado = new Span(Boolean.TRUE.equals(h.getActivo()) ? "Activo" : "Inactivo"); estado.addClassName(Boolean.TRUE.equals(h.getActivo()) ? "badge-activo" : "badge-inactivo"); return estado; }).setHeader("ESTADO");
        grid.addComponentColumn(h -> { Button detalle = new Button(VaadinIcon.EDIT.create(), e -> detalleHorario(h)); detalle.setTooltipText("Ver horario"); detalle.addThemeVariants(ButtonVariant.LUMO_TERTIARY); return detalle; }).setHeader("ACCIONES"); grid.setWidthFull();
    }
    private void configurarAusencias() {
        ausenciasGrid.addColumn(a -> a.getMedico().getNombreCompleto()).setHeader("M\u00c9DICO");
        ausenciasGrid.addColumn(AusenciaMedico::getFechaInicio).setHeader("INICIO");
        ausenciasGrid.addColumn(AusenciaMedico::getFechaFin).setHeader("FIN");
        ausenciasGrid.addColumn(AusenciaMedico::getMotivo).setHeader("MOTIVO");
        ausenciasGrid.addComponentColumn(a -> { Span estado = new Span(Boolean.TRUE.equals(a.getActivo()) ? "Activa" : "Cancelada"); estado.addClassName(Boolean.TRUE.equals(a.getActivo()) ? "badge-activo" : "badge-cancelada"); return estado; }).setHeader("ESTADO");
        ausenciasGrid.addComponentColumn(a -> { Button editar = new Button(VaadinIcon.EDIT.create(), e -> formularioAusencia(a)); editar.setTooltipText("Editar"); editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY); return editar; }).setHeader("ACCIONES");
        ausenciasGrid.setWidthFull();
    }
    private void actualizar() { List<HorarioMedico> items = empresaId == null ? List.of() : horarioService.listar(empresaId).stream().filter(h -> medicoFiltro.getValue() == null || h.getMedico().getId().equals(medicoFiltro.getValue().getId())).filter(h -> sucursalFiltro.getValue() == null || h.getSucursal().getId().equals(sucursalFiltro.getValue().getId())).toList(); grid.setItems(items); pintarSemanal(items); ausenciasGrid.setItems(empresaId == null ? List.of() : ausenciaService.listar(empresaId).stream().filter(a -> medicoFiltro.getValue() == null || a.getMedico().getId().equals(medicoFiltro.getValue().getId())).toList()); }
    private void pintarSemanal(List<HorarioMedico> items) {
        semanal.removeAll(); semanal.getStyle().set("display", "grid").set("grid-template-columns", "repeat(7, minmax(115px, 1fr))").set("gap", "0.5rem").set("padding", "1rem").set("background", "#fff").set("border-radius", "12px");
        for (int i=1;i<=7;i++) { int diaActual = i; VerticalLayout columna = new VerticalLayout(); columna.setPadding(false); columna.setSpacing(false); columna.add(new H4(diaCorto(diaActual))); items.stream().filter(h -> h.getDiaSemana()==diaActual).forEach(h -> { Button bloque = new Button(h.getHoraInicio()+" - "+h.getHoraFin(), e -> detalleHorario(h)); bloque.setTooltipText(h.getSucursal().getNombre() + (h.getConsultorio() == null ? "" : " · " + h.getConsultorio().getNombre()) + " · " + h.getDuracionCitaMinutos() + " min"); bloque.setWidthFull(); bloque.getStyle().set("font-size", "0.8rem").set("margin-bottom", "0.35rem").set("min-height", "3.25rem").set("overflow", "hidden").set("white-space", "nowrap").set("text-overflow", "ellipsis").set("background", Boolean.TRUE.equals(h.getActivo()) ? "#dbeafe" : "#e2e8f0"); columna.add(bloque); }); semanal.add(columna); }
    }
    private void formularioHorario() {
        if (empresaId == null) return; Dialog dialog = new Dialog(); dialog.setHeaderTitle("Agregar horario"); dialog.setWidth("700px");
        ComboBox<Medico> medico = selector("M\u00e9dico", medicoService.listarActivos(empresaId), Medico::getNombreCompleto); ComboBox<Sucursal> sucursal = selector("Sucursal", sucursalService.listarActivas(empresaId), Sucursal::getNombre); ComboBox<Consultorio> consultorio = selector("Consultorio", consultorioService.listarActivos(empresaId), Consultorio::getNombre); consultorio.setRequiredIndicatorVisible(false);
        sucursal.addValueChangeListener(e -> consultorio.setItems(e.getValue()==null?List.of():consultorioService.listarActivos(empresaId).stream().filter(c -> e.getValue().getId().equals(c.getSucursal().getId())).toList()));
        CheckboxGroup<Integer> dias = new CheckboxGroup<>("D\u00edas de la semana"); dias.setItems(1,2,3,4,5,6,7); dias.setItemLabelGenerator(this::dia); dias.setValue(java.util.Set.of(1)); TimePicker inicio = new TimePicker("Hora inicio"); TimePicker fin = new TimePicker("Hora fin"); inicio.setValue(LocalTime.of(8,0)); fin.setValue(LocalTime.of(12,0)); IntegerField duracion = new IntegerField("Duraci\u00f3n de consulta (minutos)"); duracion.setValue(30); duracion.setHelperText("Sugeridas: 15, 20, 30, 45 o 60 minutos"); Checkbox activo = new Checkbox("Horario activo", true);
        dialog.add(new FormLayout(medico,sucursal,consultorio,dias,inicio,fin,duracion,activo));
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> { try { HorarioMedico h = new HorarioMedico(); h.setMedico(medico.getValue()); h.setSucursal(sucursal.getValue()); h.setConsultorio(consultorio.getValue()); h.setHoraInicio(inicio.getValue()); h.setHoraFin(fin.getValue()); h.setDuracionCitaMinutos(duracion.getValue()); h.setActivo(activo.getValue()); horarioService.crearBloques(empresaId,h,dias.getValue()); dialog.close(); actualizar(); Notification.show("Horario guardado.",3000,Notification.Position.BOTTOM_START); } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE); }}); guardar.setTooltipText("Guardar"); guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); guardar.getStyle().set("background","#16a34a").set("color","white"); Button cerrar = new Button(VaadinIcon.CLOSE.create(),e->dialog.close()); cerrar.setTooltipText("Cerrar"); cerrar.getStyle().set("background","#e2e8f0").set("color","#1e293b"); dialog.getFooter().add(guardar,cerrar); dialog.open();
    }
    private void detalleHorario(HorarioMedico horario) {
        Dialog dialog = new Dialog(); dialog.setHeaderTitle("Horario: "+dia(horario.getDiaSemana())); VerticalLayout contenido = new VerticalLayout(new Span(horario.getMedico().getNombreCompleto()),new Span(horario.getHoraInicio()+" - "+horario.getHoraFin()+" · "+horario.getDuracionCitaMinutos()+" min"),new Span(horario.getSucursal().getNombre() + (horario.getConsultorio()==null?"":" · "+horario.getConsultorio().getNombre()))); contenido.add(new H4("Descansos")); descansoService.listar(empresaId,horario.getId()).forEach(d -> contenido.add(new Span(d.getHoraInicio()+" - "+d.getHoraFin()+" · "+(d.getDescripcion()==null?"":d.getDescripcion())))); dialog.add(contenido);
        Button descanso = new Button(VaadinIcon.PLUS.create(),e->formularioDescanso(horario,dialog)); descanso.setTooltipText("Agregar descanso"); Button estado = new Button(Boolean.TRUE.equals(horario.getActivo())?VaadinIcon.BAN.create():VaadinIcon.CHECK.create(), e->{ horarioService.cambiarEstado(empresaId,horario.getId(),!horario.getActivo()); dialog.close(); actualizar(); }); estado.setTooltipText(Boolean.TRUE.equals(horario.getActivo())?"Desactivar horario":"Activar horario"); estado.getStyle().set("background",Boolean.TRUE.equals(horario.getActivo())?"#dc2626":"#16a34a").set("color","white"); Button cerrar = new Button(VaadinIcon.CLOSE.create(),e->dialog.close()); cerrar.setTooltipText("Cerrar"); dialog.getFooter().add(descanso,estado,cerrar); dialog.open();
    }
    private void formularioDescanso(HorarioMedico horario, Dialog detalle) { Dialog dialog = new Dialog(); dialog.setHeaderTitle("Agregar descanso"); TimePicker inicio=new TimePicker("Hora inicio"), fin=new TimePicker("Hora fin"); TextArea descripcion=new TextArea("Descripci\u00f3n"); dialog.add(new FormLayout(inicio,fin,descripcion)); Button guardar=new Button(VaadinIcon.DISC.create(),e->{try{DescansoHorarioMedico d=new DescansoHorarioMedico();d.setHorarioId(horario.getId());d.setHoraInicio(inicio.getValue());d.setHoraFin(fin.getValue());d.setDescripcion(descripcion.getValue());descansoService.guardar(empresaId,d);dialog.close();detalle.close();detalleHorario(horario);}catch(IllegalArgumentException ex){Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE);}});guardar.setTooltipText("Guardar");guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); Button cerrar=new Button(VaadinIcon.CLOSE.create(),e->dialog.close());cerrar.setTooltipText("Cerrar");dialog.getFooter().add(guardar,cerrar);dialog.open(); }
    private void formularioAusencia(AusenciaMedico ausenciaExistente) { if(empresaId==null)return; boolean esEdicion=ausenciaExistente!=null; Dialog dialog=new Dialog();dialog.setHeaderTitle(esEdicion?"Editar ausencia":"Registrar ausencia");ComboBox<Medico> medico=selector("M\u00e9dico",medicoService.listarActivos(empresaId),Medico::getNombreCompleto);DateTimePicker inicio=new DateTimePicker("Fecha inicio"),fin=new DateTimePicker("Fecha fin");TextArea motivo=new TextArea("Motivo"); if(esEdicion){medico.setValue(ausenciaExistente.getMedico());inicio.setValue(ausenciaExistente.getFechaInicio());fin.setValue(ausenciaExistente.getFechaFin());motivo.setValue(ausenciaExistente.getMotivo()==null?"":ausenciaExistente.getMotivo());}else{inicio.setValue(LocalDateTime.now());fin.setValue(LocalDateTime.now().plusHours(1));}dialog.add(new FormLayout(medico,inicio,fin,motivo));Button guardar=new Button(VaadinIcon.DISC.create(),e->{try{AusenciaMedico a=new AusenciaMedico();a.setMedico(medico.getValue());a.setFechaInicio(inicio.getValue());a.setFechaFin(fin.getValue());a.setMotivo(motivo.getValue());if(esEdicion)ausenciaService.actualizar(empresaId,ausenciaExistente.getId(),a);else ausenciaService.guardar(empresaId,a);dialog.close();actualizar();Notification.show(esEdicion?"Ausencia actualizada.":"Ausencia registrada.");}catch(IllegalArgumentException ex){Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE);}});guardar.setTooltipText("Guardar");guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);guardar.getStyle().set("background","#16a34a").set("color","white");Button cerrar=new Button(VaadinIcon.CLOSE.create(),e->dialog.close());cerrar.setTooltipText("Cerrar");cerrar.getStyle().set("background","#e2e8f0").set("color","#1e293b");dialog.getFooter().add(guardar);if(esEdicion){Button cancelar=new Button(VaadinIcon.BAN.create(),e->confirmarCancelacionAusencia(ausenciaExistente,dialog));cancelar.setTooltipText("Cancelar ausencia");cancelar.setEnabled(Boolean.TRUE.equals(ausenciaExistente.getActivo()));cancelar.getStyle().set("background","#dc2626").set("color","white");dialog.getFooter().add(cancelar);}dialog.getFooter().add(cerrar);dialog.open(); }
    private void confirmarCancelacionAusencia(AusenciaMedico ausencia, Dialog dialogEdicion) { Dialog confirmacion=new Dialog();confirmacion.setHeaderTitle("Cancelar ausencia");confirmacion.add(new Span("\u00bfEst\u00e1 seguro que desea cancelar la ausencia?"));Button volver=new Button(VaadinIcon.ARROW_LEFT.create(),e->confirmacion.close());volver.setTooltipText("Volver");Button confirmar=new Button(VaadinIcon.BAN.create(),e->{try{ausenciaService.cancelar(empresaId,ausencia.getId());confirmacion.close();dialogEdicion.close();actualizar();Notification.show("Ausencia cancelada.",3000,Notification.Position.BOTTOM_START);}catch(IllegalArgumentException ex){Notification.show(ex.getMessage(),4000,Notification.Position.MIDDLE);}});confirmar.setTooltipText("Confirmar cancelaci\u00f3n");confirmar.getStyle().set("background","#dc2626").set("color","white");confirmacion.getFooter().add(volver,confirmar);confirmacion.open(); }
    private <T> ComboBox<T> selector(String etiqueta,List<T> items,java.util.function.Function<T,String> label){ComboBox<T> s=new ComboBox<>(etiqueta);s.setItems(items);s.setItemLabelGenerator(label::apply);s.setPlaceholder("Seleccione "+etiqueta.toLowerCase());s.setRequiredIndicatorVisible(true);return s;}
    private String dia(int d){return new String[]{"Lunes","Martes","Mi\u00e9rcoles","Jueves","Viernes","S\u00e1bado","Domingo"}[d-1];} private String diaCorto(int d){return new String[]{"LUN","MAR","MI\u00c9","JUE","VIE","S\u00c1B","DOM"}[d-1];}
}
