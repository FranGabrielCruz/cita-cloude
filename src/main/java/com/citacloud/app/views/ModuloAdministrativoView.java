package com.citacloud.app.views;
import com.vaadin.flow.component.html.*; import com.vaadin.flow.component.orderedlayout.VerticalLayout;
public abstract class ModuloAdministrativoView extends VerticalLayout { protected ModuloAdministrativoView(String titulo,String descripcion){setSizeFull();setPadding(true);add(new H2(titulo),new Paragraph(descripcion)); Div vacio=new Div(new H3("Aún no hay registros"),new Span("Los registros de la clínica aparecerán aquí cuando se creen."));vacio.addClassName("fase2-empty-state");add(vacio);} }
