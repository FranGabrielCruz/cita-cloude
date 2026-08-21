package com.citacloud.app.views.components;

import com.citacloud.app.services.CitaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.*; import java.time.format.DateTimeFormatter; import java.util.UUID;

/** Selector reutilizable: sólo muestra horas que el backend confirmó como reservables. */
public class SelectorHorariosDisponibles extends VerticalLayout {
 private final CitaService citas; private final UUID empresa; private final ComboBox<LocalTime> slots=new ComboBox<>(); private final Span resumen=new Span("Seleccione médico y fecha para consultar los horarios disponibles."); private UUID medicoActual; private LocalDate fechaActual; private LocalTime inicio,fin;
 public SelectorHorariosDisponibles(CitaService citas,UUID empresa){this.citas=citas;this.empresa=empresa;setPadding(false);setSpacing(false);setMargin(false);setWidthFull();getStyle().set("align-self","start").set("justify-content","flex-start").set("margin-top","var(--lumo-space-m)");slots.setWidthFull();slots.setPlaceholder("Seleccione un horario");slots.setItemLabelGenerator(hora->hora.format(DateTimeFormatter.ofPattern("h:mm a")));Span etiqueta=new Span("Horarios disponibles");etiqueta.getStyle().set("font-size","var(--lumo-font-size-s)").set("font-weight","500").set("margin-bottom","var(--lumo-space-xs)");resumen.getStyle().set("margin-top","var(--lumo-space-xs)");slots.addValueChangeListener(e->{inicio=e.getValue();fin=inicio==null?null:citas.obtenerHoraFinSugerida(empresa,medicoActual,fechaActual,inicio).orElse(null);resumen.setText(inicio==null?"Seleccione una hora de inicio disponible.":"✓ Horario seleccionado: "+slots.getItemLabelGenerator().apply(inicio)+" — "+(fin==null?"":slots.getItemLabelGenerator().apply(fin)));});add(etiqueta,slots,resumen);}
 public void actualizar(UUID medico,LocalDate fecha,UUID excluir){medicoActual=medico;fechaActual=fecha;inicio=null;fin=null;slots.clear();if(medico==null||fecha==null){slots.setItems(java.util.List.of());slots.setEnabled(false);resumen.setText("Seleccione médico y fecha para consultar los horarios disponibles.");return;}var horas=citas.obtenerSlotsDisponibles(empresa,medico,fecha,excluir);slots.setItems(horas);slots.setEnabled(!horas.isEmpty());resumen.setText(horas.isEmpty()?"No hay horarios disponibles para esta fecha.":"Seleccione una hora de inicio disponible.");}
 public LocalTime getInicio(){return inicio;} public LocalTime getFin(){return fin;}
}
