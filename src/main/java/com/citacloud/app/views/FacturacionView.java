package com.citacloud.app.views;

import com.citacloud.app.dto.*;
import com.citacloud.app.models.*;
import com.citacloud.app.security.*;
import com.citacloud.app.services.*;
import com.citacloud.app.util.FormatoMonto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "facturacion", layout = MainLayout.class)
@PageTitle("Facturación | CitaCloud")
@PermitAll
public class FacturacionView extends VerticalLayout {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.US);
    private final FacturacionService facturacion;
    private final FacturacionCobroService facturacionCobro;
    private final FacturaCalculadora calculadora;
    private final CajaService cajaService;
    private final SecuenciaComprobanteFiscalService secuenciasFiscales;
    private final PacienteService pacientes;
    private final SucursalService sucursales;
    private final MedicoService medicos;
    private final TenantUserDetails usuario;
    private final UUID empresaId;
    private final Grid<Factura> tabla = new Grid<>(Factura.class, false);
    private final TextField buscar = new TextField();
    private final ComboBox<String> periodo = new ComboBox<>(), estado = new ComboBox<>();
    private final ComboBox<Sucursal> sucursal = new ComboBox<>();
    private final ComboBox<Paciente> paciente = new ComboBox<>();
    private final ComboBox<Medico> medico = new ComboBox<>();
    private final DatePicker desde = new DatePicker("Desde"), hasta = new DatePicker("Hasta");
    private final Div indicadores = new Div();
    private final Span vacio = new Span();
    private final ComboBox<Integer> filas = new ComboBox<>();
    private final Span informacionPagina = new Span();
    private final Button anterior = new Button(VaadinIcon.ANGLE_LEFT.create()), siguiente = new Button(VaadinIcon.ANGLE_RIGHT.create());
    private final Span contextoCaja = new Span("Caja no configurada");
    private Caja cajaSeleccionada;
    private SesionCaja turnoSeleccionado;
    private boolean configuracionCajaIniciada;
    private int pagina;

    public FacturacionView(FacturacionService facturacion, FacturacionCobroService facturacionCobro,
                           FacturaCalculadora calculadora, CajaService cajaService,
                           SecuenciaComprobanteFiscalService secuenciasFiscales,
                           PacienteService pacientes, SucursalService sucursales, MedicoService medicos) {
        this.facturacion = facturacion; this.facturacionCobro = facturacionCobro;
        this.calculadora = calculadora; this.cajaService = cajaService; this.secuenciasFiscales = secuenciasFiscales;
        this.pacientes = pacientes;
        this.sucursales = sucursales; this.medicos = medicos;
        usuario = AuthService.getAuthenticatedUser(); empresaId = usuario == null ? null : usuario.getEmpresaId();
        setWidthFull(); setPadding(true); setSpacing(true); addClassName("facturacion-view");
        construirCabecera(); construirFiltros(); construirIndicadores(); construirTabla(); construirPaginacion();
        aplicarPeriodo("Este mes"); cargar();
        addAttachListener(e -> { if (!configuracionCajaIniciada) { configuracionCajaIniciada = true; cargarCajaNavegador(); } });
    }

    private void construirCabecera() {
        H2 titulo = new H2("FACTURACIÓN"); titulo.getStyle().set("margin", "0");
        contextoCaja.addClassName("facturacion-caja-contexto");
        Button configurarCaja = new Button(VaadinIcon.COG.create(), e -> abrirConfiguracionCaja(false)); configurarCaja.setTooltipText("Configurar caja de facturación"); configurarCaja.setAriaLabel("Configurar caja de facturación"); configurarCaja.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        HorizontalLayout tituloCaja = new HorizontalLayout(titulo, contextoCaja, configurarCaja); tituloCaja.setAlignItems(Alignment.CENTER); tituloCaja.setSpacing(true); tituloCaja.getStyle().set("flex-wrap", "wrap");
        Paragraph subtitulo = new Paragraph("Gestión y seguimiento de facturas"); subtitulo.getStyle().set("margin", ".2rem 0 0").set("color", "var(--lumo-secondary-text-color)");
        Button nueva = iconoPrincipal(VaadinIcon.PLUS, "Nueva factura", e -> formulario(null));
        nueva.setVisible(puede("BILLING_CREATE"));
        HorizontalLayout cabecera = new HorizontalLayout(new Div(tituloCaja, subtitulo), nueva);
        cabecera.setWidthFull(); cabecera.setAlignItems(Alignment.CENTER); cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(cabecera);
    }

    private void construirFiltros() {
        buscar.setPlaceholder("Buscar factura, paciente..."); buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true); buscar.setValueChangeMode(ValueChangeMode.LAZY); buscar.setValueChangeTimeout(350);
        buscar.addValueChangeListener(e -> reiniciarYCargar()); buscar.setWidthFull();
        periodo.setLabel("Período"); periodo.setItems("Hoy", "Esta semana", "Este mes", "Últimos 30 días", "Este trimestre", "Este año", "Personalizado");
        periodo.setValue("Este mes"); periodo.addValueChangeListener(e -> { aplicarPeriodo(e.getValue()); reiniciarYCargar(); });
        estado.setLabel("Estado"); estado.setItems("TODOS", "BORRADOR", "PENDIENTE", "PARCIAL", "PAGADA", "ANULADA"); estado.setValue("TODOS"); estado.addValueChangeListener(e -> reiniciarYCargar());
        sucursal.setLabel("Sucursal"); sucursal.setItemLabelGenerator(Sucursal::getNombre); sucursal.setClearButtonVisible(true); sucursal.addValueChangeListener(e -> reiniciarYCargar());
        paciente.setLabel("Paciente"); paciente.setItemLabelGenerator(p -> p.getNombreCompleto() + " · " + p.getNumeroExpediente()); paciente.setClearButtonVisible(true); paciente.addValueChangeListener(e -> reiniciarYCargar());
        medico.setLabel("Médico"); medico.setItemLabelGenerator(Medico::getNombreCompleto); medico.setClearButtonVisible(true); medico.addValueChangeListener(e -> reiniciarYCargar());
        desde.setVisible(false); hasta.setVisible(false); desde.addValueChangeListener(e -> reiniciarYCargar()); hasta.addValueChangeListener(e -> reiniciarYCargar());
        if (empresaId != null) {
            List<Sucursal> listaSucursales = sucursales.listarActivas(empresaId); sucursal.setItems(listaSucursales);
            paciente.setItems(pacientes.listarActivos(empresaId)); medico.setItems(medicos.listarActivos(empresaId));
            if (listaSucursales.size() == 1) sucursal.setValue(listaSucursales.getFirst());
        }
        HorizontalLayout filtros = new HorizontalLayout(buscar, periodo, estado, sucursal, paciente, medico, desde, hasta);
        filtros.setWidthFull(); filtros.setAlignItems(Alignment.END); filtros.getStyle().set("flex-wrap", "wrap").set("gap", ".8rem");
        buscar.getStyle().set("flex", "2 1 260px");
        for (Component campo : List.of(periodo, estado, sucursal, paciente, medico, desde, hasta)) campo.getElement().getStyle().set("flex", "1 1 155px");
        add(filtros);
    }

    private void construirIndicadores() {
        indicadores.setWidthFull(); indicadores.addClassName("facturacion-indicadores");
        indicadores.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit,minmax(180px,1fr))").set("gap", "1rem");
        add(indicadores);
    }

    private void construirTabla() {
        tabla.addColumn(Factura::getNumero).setHeader("Factura").setAutoWidth(true).setSortable(true);
        tabla.addColumn(f -> fecha(f.getFecha())).setHeader("Fecha").setAutoWidth(true).setSortable(true);
        tabla.addColumn(f -> f.getPaciente() == null ? "-" : f.getPaciente().getNombreCompleto()).setHeader("Paciente").setFlexGrow(1);
        tabla.addColumn(f -> dinero(f.getTotal())).setHeader("Total").setAutoWidth(true);
        tabla.addColumn(f -> dinero(f.getMontoPagado())).setHeader("Pagado").setAutoWidth(true);
        tabla.addColumn(f -> dinero(f.getSaldo())).setHeader("Pendiente").setAutoWidth(true);
        tabla.addComponentColumn(f -> badge(f.getEstado())).setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(f -> estadoEcf(f.getEstadoEcf())).setHeader("e-CF").setAutoWidth(true);
        tabla.addComponentColumn(this::accionesTabla).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        tabla.setWidthFull(); tabla.setAllRowsVisible(true); tabla.addClassName("facturacion-tabla");
        vacio.getStyle().set("display", "block").set("padding", "2rem").set("text-align", "center").set("color", "var(--lumo-secondary-text-color)");
        add(tabla, vacio);
    }

    private void construirPaginacion() {
        filas.setAriaLabel("Registros por página"); filas.setItems(10, 20, 50, 100); filas.setValue(10); filas.setWidth("90px");
        filas.addValueChangeListener(e -> reiniciarYCargar()); anterior.setTooltipText("Página anterior"); siguiente.setTooltipText("Página siguiente");
        anterior.addThemeVariants(ButtonVariant.LUMO_TERTIARY); siguiente.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        anterior.addClickListener(e -> { if (pagina > 0) { pagina--; cargar(); } }); siguiente.addClickListener(e -> { pagina++; cargar(); });
        HorizontalLayout configuracion = new HorizontalLayout(new Span("Ver"), filas, new Span("por página")); configuracion.setAlignItems(Alignment.CENTER);
        HorizontalLayout navegacion = new HorizontalLayout(informacionPagina, anterior, siguiente); navegacion.setAlignItems(Alignment.CENTER);
        HorizontalLayout paginacion = new HorizontalLayout(configuracion, navegacion); paginacion.setWidthFull(); paginacion.setAlignItems(Alignment.CENTER); paginacion.setJustifyContentMode(JustifyContentMode.BETWEEN); paginacion.addClassName("facturacion-paginacion");
        add(paginacion);
    }

    private void cargar() {
        if (empresaId == null) return;
        try {
            FacturaFiltro filtro = filtro(); int tamanio = filas.getValue() == null ? 10 : filas.getValue();
            Page<Factura> resultado = facturacion.buscarPagina(empresaId, filtro, pagina, tamanio);
            if (pagina >= resultado.getTotalPages() && resultado.getTotalPages() > 0) { pagina = resultado.getTotalPages() - 1; resultado = facturacion.buscarPagina(empresaId, filtro, pagina, tamanio); }
            tabla.setItems(resultado.getContent()); boolean sinDatos = resultado.getTotalElements() == 0; tabla.setVisible(!sinDatos); vacio.setVisible(sinDatos);
            vacio.setText(filtrosActivos() ? "No se encontraron facturas para los filtros seleccionados." : "No se encontraron facturas.");
            long desdeRegistro = sinDatos ? 0 : (long) pagina * tamanio + 1; long hastaRegistro = Math.min((long) (pagina + 1) * tamanio, resultado.getTotalElements());
            informacionPagina.setText(sinDatos ? "Sin registros" : "Mostrando " + desdeRegistro + "-" + hastaRegistro + " de " + resultado.getTotalElements());
            anterior.setEnabled(pagina > 0); siguiente.setEnabled(pagina + 1 < resultado.getTotalPages());
            pintarIndicadores(facturacion.resumen(empresaId, filtro));
        } catch (Exception e) { aviso(e); }
    }

    private void pintarIndicadores(FacturacionService.Resumen resumen) {
        indicadores.removeAll(); indicadores.add(tarjeta("FACTURADO", dinero(resumen.facturado())), tarjeta("FACTURAS", String.valueOf(resumen.facturas())), tarjeta("PENDIENTE", dinero(resumen.pendiente())), tarjeta("PAGADAS", String.valueOf(resumen.pagadas())));
    }

    private void formulario(Factura existente) {
        if (empresaId == null || (existente == null && !puede("BILLING_CREATE"))) return;
        if (existente != null && !puede("BILLING_EDIT_DRAFT")) return;
        if (cajaSeleccionada == null) { abrirConfiguracionCaja(true); return; }
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle(existente == null ? "NUEVA FACTURA" : "EDITAR " + existente.getNumero()); dialogo.setWidth("min(1180px, 97vw)"); dialogo.setMaxHeight("94vh");
        ComboBox<Sucursal> campoSucursal = new ComboBox<>("Sucursal *"); List<Sucursal> listaSucursales = sucursales.listarActivas(empresaId); campoSucursal.setItems(listaSucursales); campoSucursal.setItemLabelGenerator(Sucursal::getNombre);
        ComboBox<Paciente> campoPaciente = new ComboBox<>("Paciente *"); campoPaciente.setItems(pacientes.listarActivos(empresaId)); campoPaciente.setItemLabelGenerator(p -> p.getNombreCompleto() + " · " + p.getNumeroExpediente());
        ComboBox<Medico> campoMedico = new ComboBox<>("Médico"); campoMedico.setItems(medicos.listarActivos(empresaId)); campoMedico.setItemLabelGenerator(Medico::getNombreCompleto); campoMedico.setClearButtonVisible(true);
        DatePicker campoFecha = new DatePicker("Fecha *"); campoFecha.setValue(existente == null ? LocalDate.now() : existente.getFecha());
        TextField numero = new TextField("Número"); numero.setReadOnly(true); numero.setValue(existente == null ? "Generado al guardar" : existente.getNumero());
        ComboBox<SecuenciaComprobanteFiscal> comprobante = new ComboBox<>("Tipo/comprobante fiscal");
        List<SecuenciaComprobanteFiscal> secuenciasDisponibles = secuenciasFiscales.listarActivas(empresaId);
        comprobante.setItems(secuenciasDisponibles); comprobante.setItemLabelGenerator(SecuenciaComprobanteFiscal::getNombre); comprobante.setClearButtonVisible(true);
        comprobante.setPlaceholder(secuenciasDisponibles.isEmpty() ? "Configúrelo en Configuración" : "Sin comprobante fiscal");
        TextArea observacion = new TextArea("Observaciones"); observacion.setMinHeight("86px");
        if (existente != null) { campoSucursal.setValue(existente.getSucursal()); campoPaciente.setValue(existente.getPaciente()); campoMedico.setValue(existente.getMedico()); secuenciasDisponibles.stream().filter(s -> Objects.equals(s.getTipo(), existente.getTipoComprobante())).findFirst().ifPresent(comprobante::setValue); observacion.setValue(texto(existente.getObservacion())); }
        else if (cajaSeleccionada != null) campoSucursal.setValue(cajaSeleccionada.getSucursal());
        else if (listaSucursales.size() == 1) campoSucursal.setValue(listaSucursales.getFirst());
        FormLayout cabecera = new FormLayout(numero, campoFecha, campoSucursal, campoPaciente, campoMedico, comprobante, observacion); cabecera.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("700px", 3)); cabecera.setColspan(observacion, 3);

        List<LineaEdicion> lineas = new ArrayList<>();
        if (existente != null) facturacion.detalles(empresaId, existente.getId()).forEach(d -> lineas.add(LineaEdicion.desde(d)));
        Grid<LineaEdicion> detalle = new Grid<>(LineaEdicion.class, false); detalle.addColumn(LineaEdicion::descripcion).setHeader("Descripción").setFlexGrow(1); detalle.addColumn(LineaEdicion::tipo).setHeader("Tipo"); detalle.addColumn(l -> numero(l.cantidad())).setHeader("Cantidad"); detalle.addColumn(l -> dinero(l.precio())).setHeader("Precio"); detalle.addColumn(l -> dinero(l.calculo().descuento())).setHeader("Descuento"); detalle.addColumn(l -> dinero(l.calculo().impuesto())).setHeader("Impuesto"); detalle.addColumn(l -> dinero(l.calculo().total())).setHeader("Total"); detalle.addComponentColumn(l -> icono(VaadinIcon.TRASH, "Eliminar línea", e -> { lineas.remove(l); detalle.setItems(lineas); })).setHeader("Acción"); detalle.setWidthFull(); detalle.setAllRowsVisible(true); detalle.setItems(lineas);
        Div resumen = new Div(); Runnable actualizarResumen = () -> pintarResumen(resumen, lineas); actualizarResumen.run();
        Button agregar = new Button(VaadinIcon.PLUS.create(), e -> agregarLinea(lineas, detalle, actualizarResumen)); agregar.setTooltipText("Agregar servicio o producto"); agregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout tituloDetalle = new HorizontalLayout(new H3("DETALLE"), agregar); tituloDetalle.setWidthFull(); tituloDetalle.setAlignItems(Alignment.CENTER); tituloDetalle.setJustifyContentMode(JustifyContentMode.BETWEEN);
        VerticalLayout contenido = new VerticalLayout(new H3("1. Información de la factura"), cabecera, tituloDetalle, detalle, new H3("RESUMEN"), resumen); contenido.setPadding(false); contenido.setSpacing(true);
        dialogo.add(contenido);
        String claveIdempotencia = UUID.randomUUID().toString();
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> {
            try {
                FacturaSolicitud solicitud = new FacturaSolicitud(campoSucursal.getValue() == null ? null : campoSucursal.getValue().getId(), campoPaciente.getValue() == null ? null : campoPaciente.getValue().getId(), campoMedico.getValue() == null ? null : campoMedico.getValue().getId(), campoFecha.getValue(), comprobante.getValue() == null ? null : comprobante.getValue().getTipo(), null, null, observacion.getValue(), claveIdempotencia, lineas.stream().map(LineaEdicion::solicitud).toList());
                Factura guardada = existente == null ? facturacion.crearBorrador(empresaId, solicitud) : facturacion.guardarBorrador(empresaId, existente.getId(), solicitud);
                dialogo.close(); cargar(); Notification.show("Borrador " + guardada.getNumero() + " guardado correctamente."); detalle(guardada);
            } catch (Exception ex) { aviso(ex); }
        });
        guardar.setTooltipText("Guardar"); guardar.addThemeVariants(ButtonVariant.LUMO_SUCCESS); guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff").set("min-width", "48px").set("height", "48px");
        dialogo.getFooter().add(guardar, botonCancelar(dialogo)); dialogo.open();
    }

    private void agregarLinea(List<LineaEdicion> lineas, Grid<LineaEdicion> detalle, Runnable actualizarResumen) {
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("Agregar línea"); dialogo.setWidth("min(720px, 96vw)");
        List<ItemFactura> catalogo = new ArrayList<>(); facturacion.serviciosFacturables(empresaId).forEach(s -> catalogo.add(new ItemFactura("SERVICIO", s.getId(), s.getCodigo(), s.getNombre(), s.getPrecio(), s.getTasaImpuesto()))); facturacion.productosFacturables(empresaId).forEach(p -> catalogo.add(new ItemFactura("PRODUCTO", p.getId(), p.getCodigo(), p.getNombre(), p.getPrecioVenta(), p.getTasaImpuesto())));
        ComboBox<String> tipo = new ComboBox<>("Tipo *", "SERVICIO", "PRODUCTO"); tipo.setValue("SERVICIO");
        ComboBox<ItemFactura> item = new ComboBox<>("Servicio/producto *"); item.setItemLabelGenerator(i -> i.codigo() + " · " + i.nombre());
        Runnable filtrar = () -> { item.clear(); item.setItems(catalogo.stream().filter(i -> i.tipo().equals(tipo.getValue())).toList()); }; filtrar.run(); tipo.addValueChangeListener(e -> filtrar.run());
        TextField cantidad = montoCampo("Cantidad *", "1.00"), precio = montoCampo("Precio unitario *", "");
        ComboBox<String> tipoDescuento = new ComboBox<>("Descuento", "NINGUNO", "PORCENTAJE", "MONTO"); tipoDescuento.setValue("NINGUNO");
        TextField descuento = montoCampo("Valor descuento", "0.00"); descuento.setEnabled(false);
        item.addValueChangeListener(e -> { if (e.getValue() != null) precio.setValue(numero(e.getValue().precio())); });
        precio.setReadOnly(!puede("BILLING_CHANGE_PRICE")); tipoDescuento.setEnabled(puede("BILLING_APPLY_DISCOUNT"));
        tipoDescuento.addValueChangeListener(e -> { descuento.setEnabled(puede("BILLING_APPLY_DISCOUNT") && !"NINGUNO".equals(e.getValue())); if (!descuento.isEnabled()) descuento.setValue("0.00"); });
        FormLayout formulario = new FormLayout(tipo, item, cantidad, precio, tipoDescuento, descuento); formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("580px", 2));
        Button aceptar = new Button(VaadinIcon.CHECK.create(), e -> { try { ItemFactura seleccionado = item.getValue(); if (seleccionado == null) throw new IllegalArgumentException("Seleccione un servicio o producto."); BigDecimal cantidadValor = FormatoMonto.parse(cantidad.getValue()), precioValor = FormatoMonto.parse(precio.getValue()), descuentoValor = FormatoMonto.parse(descuento.getValue()); FacturaCalculadora.Linea calculo = calculadora.calcularLinea(cantidadValor, precioValor, tipoDescuento.getValue(), descuentoValor, seleccionado.tasa()); lineas.add(new LineaEdicion(seleccionado.tipo(), seleccionado.id(), seleccionado.nombre(), cantidadValor, precioValor, tipoDescuento.getValue(), descuentoValor, seleccionado.tasa(), calculo)); detalle.setItems(lineas); actualizarResumen.run(); dialogo.close(); } catch (Exception ex) { aviso(ex); } });
        aceptar.setTooltipText("Agregar línea"); aceptar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialogo.add(formulario); dialogo.getFooter().add(aceptar, botonCancelar(dialogo)); dialogo.open();
    }

    private void detalle(Factura facturaInicial) {
        Factura factura;
        try { factura = facturacion.obtener(empresaId, facturaInicial.getId()); } catch (Exception e) { aviso(e); return; }
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("FACTURA " + factura.getNumero()); dialogo.setWidth("min(1050px, 97vw)"); dialogo.setMaxHeight("94vh");
        FormLayout datos = new FormLayout(); datos.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("650px", 3));
        datos.addFormItem(new Span(fecha(factura.getFecha())), "Fecha"); datos.addFormItem(badge(factura.getEstado()), "Estado"); datos.addFormItem(new Span(estadoEcf(factura.getEstadoEcf())), "e-CF"); datos.addFormItem(new Span(factura.getPaciente().getNombreCompleto()), "Paciente"); datos.addFormItem(new Span(factura.getMedico() == null ? "No aplica" : factura.getMedico().getNombreCompleto()), "Médico"); datos.addFormItem(new Span(factura.getSucursal() == null ? "-" : factura.getSucursal().getNombre()), "Sucursal"); datos.addFormItem(new Span(comprobanteFiscal(factura)), "Comprobante fiscal");
        List<DetalleFactura> lineas = facturacion.detalles(empresaId, factura.getId()); Grid<DetalleFactura> grid = new Grid<>(DetalleFactura.class, false); grid.addColumn(DetalleFactura::getDescripcion).setHeader("Descripción").setFlexGrow(1); grid.addColumn(d -> numero(d.getCantidad())).setHeader("Cant."); grid.addColumn(d -> dinero(d.getPrecio())).setHeader("Precio"); grid.addColumn(d -> dinero(d.getDescuento())).setHeader("Descuento"); grid.addColumn(d -> dinero(d.getImpuesto())).setHeader("Impuesto"); grid.addColumn(d -> dinero(d.getImporte())).setHeader("Total"); grid.setItems(lineas); grid.setAllRowsVisible(true); grid.setWidthFull();
        Div resumen = resumenFactura(factura); List<Pago> listaPagos = facturacion.pagos(empresaId, factura.getId()); Grid<Pago> pagos = new Grid<>(Pago.class, false); pagos.addColumn(p -> texto(p.getNumero())).setHeader("Pago"); pagos.addColumn(p -> p.getCreadoEn() == null ? fecha(p.getFecha()) : p.getCreadoEn().format(FECHA_HORA)).setHeader("Fecha"); pagos.addColumn(Pago::getMetodoPago).setHeader("Método"); pagos.addColumn(p -> dinero(p.getMonto())).setHeader("Monto"); pagos.addColumn(Pago::getEstado).setHeader("Estado"); pagos.setItems(listaPagos); pagos.setAllRowsVisible(true); pagos.setVisible(!listaPagos.isEmpty());
        VerticalLayout contenido = new VerticalLayout(datos, new H3("DETALLE"), grid, new H3("RESUMEN"), resumen, new H3("PAGOS"), pagos); contenido.setPadding(false); dialogo.add(contenido);
        HorizontalLayout acciones = new HorizontalLayout(); acciones.addClassName("factura-footer-acciones"); acciones.setWidthFull(); acciones.setAlignItems(Alignment.CENTER); acciones.setJustifyContentMode(JustifyContentMode.END);
        if (FacturacionService.BORRADOR.equals(factura.getEstado()) && puede("BILLING_EDIT_DRAFT")) acciones.add(botonAccionFactura(VaadinIcon.EDIT, "Editar borrador", "factura-footer-editar", e -> { dialogo.close(); formulario(factura); }));
        if (FacturacionService.BORRADOR.equals(factura.getEstado()) && puede("BILLING_ISSUE")) acciones.add(botonAccionFactura(VaadinIcon.CHECK_CIRCLE, "Emitir factura y registrar pago", "factura-footer-emitir", e -> emitirYCobrar(dialogo, factura)));
        if (FacturacionService.BORRADOR.equals(factura.getEstado()) && puede("BILLING_ISSUE")) acciones.add(botonAccionFactura(VaadinIcon.CLOCK, "Emitir y dejar pendiente", "factura-footer-emitir", e -> dejarPendiente(dialogo, factura)));
        if (Set.of(FacturacionService.PENDIENTE, FacturacionService.PARCIAL).contains(factura.getEstado()) && puede("BILLING_REGISTER_PAYMENT")) acciones.add(botonAccionFactura(VaadinIcon.CREDIT_CARD, "Registrar pago", "factura-footer-pago", e -> getUI().ifPresent(ui -> ui.navigate("pagos?factura=" + factura.getId()))));
        if (!FacturacionService.BORRADOR.equals(factura.getEstado()) && puede("BILLING_PRINT")) { Anchor pdf = new Anchor("/facturas/" + factura.getId() + "/pdf", VaadinIcon.PRINT.create()); pdf.setTarget("_blank"); pdf.getElement().setAttribute("title", "Ver/Imprimir PDF"); pdf.getElement().setAttribute("aria-label", "Ver/Imprimir PDF"); pdf.addClassNames("accion-icono", "factura-footer-boton", "factura-footer-imprimir"); acciones.add(pdf); }
        if (!Set.of(FacturacionService.BORRADOR, FacturacionService.ANULADA).contains(factura.getEstado()) && puede("BILLING_VOID")) acciones.add(botonAccionFactura(VaadinIcon.BAN, "Anular factura", "factura-footer-anular", e -> anular(dialogo, factura)));
        acciones.add(botonCancelar(dialogo)); dialogo.getFooter().add(acciones); dialogo.open();
    }

    private void anular(Dialog detalle, Factura factura) {
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("ANULAR FACTURA"); dialogo.setWidth("min(620px,96vw)");
        ComboBox<String> motivo = new ComboBox<>("Motivo *", "Error de facturación", "Factura duplicada", "Servicio no prestado", "Otro"); TextArea observacion = new TextArea("Observación"); observacion.setMinHeight("100px");
        FormLayout form = new FormLayout(new Span("Factura: " + factura.getNumero()), motivo, observacion); form.setColspan(observacion, 2);
        Button confirmar = new Button(VaadinIcon.CHECK.create(), e -> { try { facturacion.anular(empresaId, factura.getId(), motivo.getValue(), observacion.getValue()); dialogo.close(); detalle.close(); cargar(); Notification.show("Factura anulada correctamente."); } catch (Exception ex) { aviso(ex); } }); confirmar.setTooltipText("Confirmar anulación"); confirmar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialogo.add(form); dialogo.getFooter().add(confirmar, botonCancelar(dialogo)); dialogo.open();
    }

    private void emitirYCobrar(Dialog detalle, Factura factura) {
        SesionCaja turno;
        try { turno = turnoFacturacionRequerido(factura); } catch (Exception ex) { actualizarContextoCaja(); aviso(ex); return; }
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("EMITIR Y REGISTRAR PAGO"); dialogo.setWidth("min(620px,96vw)");
        Span total = new Span("Total a cobrar: " + dinero(factura.getTotal())); total.getStyle().set("font-weight", "700");
        TextField caja = new TextField("Caja"); caja.setReadOnly(true);
        caja.setValue(cajaSeleccionada.getNombre() + " · " + turno.getNumero());
        ComboBox<String> metodo = new ComboBox<>("Método de pago *", "EFECTIVO", "TARJETA", "TRANSFERENCIA", "CHEQUE", "OTRO"); metodo.setValue("EFECTIVO");
        TextField efectivo = montoCampo("Efectivo recibido *", numero(factura.getTotal())); TextField referencia = new TextField("Referencia");
        metodo.addValueChangeListener(e -> efectivo.setVisible("EFECTIVO".equals(e.getValue())));
        FormLayout form = new FormLayout(total, caja, metodo, efectivo, referencia); form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("520px", 2));
        Button confirmar = new Button(VaadinIcon.CHECK.create(), e -> {
            try {
                BigDecimal recibido = "EFECTIVO".equals(metodo.getValue()) ? FormatoMonto.parse(efectivo.getValue()) : null;
                FacturacionCobroService.Resultado resultado = facturacionCobro.emitirYCobrar(empresaId, factura.getId(), cajaSeleccionada.getId(), metodo.getValue(), recibido, referencia.getValue());
                dialogo.close(); detalle.close(); cargar();
                getUI().ifPresent(ui -> ui.getPage().open("/facturas/" + resultado.factura().getId() + "/pdf", "_blank"));
                Notification.show("Factura emitida y pago " + resultado.pago().getNumero() + " registrado correctamente.");
            } catch (Exception ex) { actualizarContextoCaja(); aviso(ex); }
        });
        confirmar.setTooltipText("Emitir, cobrar y abrir PDF"); confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialogo.add(form); dialogo.getFooter().add(confirmar, botonCancelar(dialogo)); dialogo.open();
    }

    private void dejarPendiente(Dialog detalle, Factura factura) {
        try {
            turnoFacturacionRequerido(factura);
            Factura emitida = facturacion.emitir(empresaId, factura.getId(), cajaSeleccionada.getId());
            detalle.close(); cargar();
            getUI().ifPresent(ui -> ui.getPage().open("/facturas/" + emitida.getId() + "/pdf", "_blank"));
            Notification.show("Factura emitida y dejada pendiente de pago.");
        } catch (Exception ex) { actualizarContextoCaja(); aviso(ex); }
    }

    private Component accionesTabla(Factura factura) {
        Button ver = icono(VaadinIcon.EYE, "Ver factura", e -> detalle(factura));
        if (FacturacionService.BORRADOR.equals(factura.getEstado()) || !puede("BILLING_PRINT")) return ver;
        Anchor imprimir = new Anchor("/facturas/" + factura.getId() + "/pdf", VaadinIcon.PRINT.create()); imprimir.setTarget("_blank"); imprimir.getElement().setAttribute("title", "Imprimir factura"); imprimir.addClassName("accion-icono");
        return new HorizontalLayout(ver, imprimir);
    }

    private void cargarCajaNavegador() {
        if (empresaId == null) return;
        List<Caja> disponibles = cajasDisponibles();
        getUI().ifPresent(ui -> ui.getPage().executeJs("return window.localStorage.getItem($0)", claveCajaNavegador())
                .then(String.class, valor -> {
                    Caja guardada = null;
                    if (valor != null && !valor.isBlank()) {
                        try { UUID id = UUID.fromString(valor); guardada = disponibles.stream().filter(c -> id.equals(c.getId())).findFirst().orElse(null); }
                        catch (IllegalArgumentException ignored) { }
                    }
                    if (guardada == null) abrirConfiguracionCaja(true);
                    else seleccionarCaja(guardada, false);
                }));
    }

    private void abrirConfiguracionCaja(boolean obligatoria) {
        List<Caja> disponibles = cajasDisponibles();
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle("CONFIGURAR CAJA DE FACTURACIÓN"); dialogo.setWidth("min(560px,95vw)");
        dialogo.setCloseOnEsc(!obligatoria); dialogo.setCloseOnOutsideClick(!obligatoria);
        if (disponibles.isEmpty()) {
            Paragraph mensaje = new Paragraph("No hay cajas activas disponibles para este usuario. Crea o asigna una caja antes de facturar.");
            Button administrar = new Button("Ir a Caja", VaadinIcon.CASH.create(), e -> { dialogo.close(); getUI().ifPresent(ui -> ui.navigate("caja")); }); administrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialogo.add(mensaje); dialogo.getFooter().add(administrar); if (!obligatoria) dialogo.getFooter().add(botonCancelar(dialogo)); dialogo.open(); return;
        }
        ComboBox<Caja> caja = new ComboBox<>("Caja *"); caja.setItems(disponibles); caja.setWidthFull();
        caja.setItemLabelGenerator(c -> c.getNombre() + " · " + c.getSucursal().getNombre());
        if (cajaSeleccionada != null) disponibles.stream().filter(c -> Objects.equals(c.getId(), cajaSeleccionada.getId())).findFirst().ifPresent(caja::setValue);
        Paragraph ayuda = new Paragraph("Esta caja quedará guardada en este navegador y se utilizará cada vez que abras Facturación en esta máquina."); ayuda.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Button guardar = new Button(VaadinIcon.DISC.create(), e -> { if (caja.getValue() == null) { Notification.show("Seleccione una caja."); return; } seleccionarCaja(caja.getValue(), true); dialogo.close(); });
        guardar.setTooltipText("Guardar caja"); guardar.setAriaLabel("Guardar caja"); guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialogo.add(new VerticalLayout(caja, ayuda)); dialogo.getFooter().add(guardar); if (!obligatoria) dialogo.getFooter().add(botonCancelar(dialogo)); dialogo.open();
    }

    private List<Caja> cajasDisponibles() {
        if (empresaId == null) return List.of();
        Map<UUID, Caja> unicas = new LinkedHashMap<>();
        for (Sucursal sede : sucursales.listarActivas(empresaId)) cajaService.autorizadas(empresaId, sede.getId()).forEach(c -> unicas.put(c.getId(), c));
        return new ArrayList<>(unicas.values());
    }

    private void seleccionarCaja(Caja caja, boolean guardarEnNavegador) {
        cajaSeleccionada = caja;
        actualizarContextoCaja();
        if (guardarEnNavegador) getUI().ifPresent(ui -> ui.getPage().executeJs("window.localStorage.setItem($0,$1)", claveCajaNavegador(), caja.getId().toString()));
    }

    private void actualizarContextoCaja() {
        turnoSeleccionado = cajaSeleccionada == null ? null : cajaService.sesionActivaCaja(empresaId, cajaSeleccionada.getId()).orElse(null);
        if (cajaSeleccionada == null) {
            contextoCaja.setText("Caja no configurada"); contextoCaja.getElement().setAttribute("theme", "badge error");
        } else if (turnoSeleccionado == null) {
            contextoCaja.setText(cajaSeleccionada.getNombre() + " · Sin turno abierto"); contextoCaja.getElement().setAttribute("theme", "badge error");
        } else {
            contextoCaja.setText(cajaSeleccionada.getNombre() + " · Turno " + turnoSeleccionado.getNumero()); contextoCaja.getElement().setAttribute("theme", "badge success");
        }
    }

    private SesionCaja turnoFacturacionRequerido(Factura factura) {
        if (cajaSeleccionada == null) throw new IllegalArgumentException("Configure la caja de facturación antes de emitir.");
        SesionCaja turno = cajaService.sesionActivaRequerida(empresaId, factura.getSucursal().getId(), cajaSeleccionada.getId());
        turnoSeleccionado = turno; actualizarContextoCaja(); return turno;
    }

    private String claveCajaNavegador() { return "citacloud.facturacion.caja." + empresaId; }

    private FacturaFiltro filtro() { return new FacturaFiltro(buscar.getValue(), desde.getValue(), hasta.getValue(), estado.getValue(), sucursal.getValue() == null ? null : sucursal.getValue().getId(), paciente.getValue() == null ? null : paciente.getValue().getId(), medico.getValue() == null ? null : medico.getValue().getId()); }
    private void aplicarPeriodo(String valor) { LocalDate hoy = LocalDate.now(); boolean personalizado = "Personalizado".equals(valor); desde.setVisible(personalizado); hasta.setVisible(personalizado); if (personalizado) return; switch (valor == null ? "Este mes" : valor) { case "Hoy" -> { desde.setValue(hoy); hasta.setValue(hoy); } case "Esta semana" -> { desde.setValue(hoy.with(java.time.DayOfWeek.MONDAY)); hasta.setValue(hoy.with(java.time.DayOfWeek.SUNDAY)); } case "Últimos 30 días" -> { desde.setValue(hoy.minusDays(29)); hasta.setValue(hoy); } case "Este trimestre" -> { int mes = ((hoy.getMonthValue() - 1) / 3) * 3 + 1; desde.setValue(LocalDate.of(hoy.getYear(), mes, 1)); hasta.setValue(desde.getValue().plusMonths(3).minusDays(1)); } case "Este año" -> { desde.setValue(LocalDate.of(hoy.getYear(), 1, 1)); hasta.setValue(LocalDate.of(hoy.getYear(), 12, 31)); } default -> { desde.setValue(hoy.withDayOfMonth(1)); hasta.setValue(hoy.withDayOfMonth(1).plusMonths(1).minusDays(1)); } } }
    private void reiniciarYCargar() { pagina = 0; cargar(); }
    private boolean filtrosActivos() { return !texto(buscar.getValue()).isBlank() || !"TODOS".equals(estado.getValue()) || sucursal.getValue() != null || paciente.getValue() != null || medico.getValue() != null; }
    private boolean puede(String permiso) { if (usuario == null) return false; return usuario.getAuthorities().stream().anyMatch(a -> Set.of("ROLE_ADMINISTRADOR", "ROLE_SUPERADMIN", permiso).contains(a.getAuthority())); }
    private Component tarjeta(String titulo, String valor) { Div tarjeta = new Div(new Span(titulo), new H3(valor)); tarjeta.addClassName("facturacion-indicador"); tarjeta.getStyle().set("padding", "1rem 1.15rem").set("border-radius", "var(--lumo-border-radius-l)").set("background", "var(--lumo-contrast-5pct)").set("border", "1px solid var(--lumo-contrast-10pct)"); return tarjeta; }
    private Div resumenFactura(Factura f) { Div d = new Div(); d.addClassName("facturacion-resumen"); d.add(new Paragraph("Subtotal: " + dinero(f.getSubtotal())), new Paragraph("Descuento: " + dinero(f.getDescuento())), new Paragraph("Impuestos: " + dinero(f.getImpuestos())), new H3("TOTAL: " + dinero(f.getTotal())), new Paragraph("Pagado: " + dinero(f.getMontoPagado())), new Paragraph("Pendiente: " + dinero(f.getSaldo()))); return d; }
    private void pintarResumen(Div contenedor, List<LineaEdicion> lineas) { FacturaCalculadora.Totales t = calculadora.totalizar(lineas.stream().map(LineaEdicion::calculo).toList()); contenedor.removeAll(); contenedor.add(new Paragraph("Subtotal: " + dinero(t.subtotal())), new Paragraph("Descuento: " + dinero(t.descuento())), new Paragraph("Impuestos: " + dinero(t.impuestos())), new H3("TOTAL: " + dinero(t.total()))); }
    private Span badge(String valor) { Span badge = new Span(valor); badge.getElement().getThemeList().add("badge"); String color = switch (valor) { case "PAGADA" -> "success"; case "PARCIAL", "PENDIENTE" -> "contrast"; case "ANULADA" -> "error"; default -> "primary"; }; badge.getElement().getThemeList().add(color); return badge; }
    private Button iconoPrincipal(VaadinIcon icono, String tooltip, com.vaadin.flow.component.ComponentEventListener<ClickEvent<Button>> listener) { Button b = new Button(icono.create(), listener); b.setTooltipText(tooltip); b.addThemeVariants(ButtonVariant.LUMO_PRIMARY); b.getStyle().set("min-width", "48px").set("height", "48px").set("background-color", "#2563eb").set("color", "#ffffff"); return b; }
    private Button icono(VaadinIcon icono, String tooltip, com.vaadin.flow.component.ComponentEventListener<ClickEvent<Button>> listener) { Button b = new Button(icono.create(), listener); b.setTooltipText(tooltip); b.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE); b.getStyle().set("background", "transparent").set("border", "none").set("box-shadow", "none").set("min-width", "auto"); return b; }
    private Button botonAccionFactura(VaadinIcon icono, String tooltip, String clase, com.vaadin.flow.component.ComponentEventListener<ClickEvent<Button>> listener) { Button b = new Button(icono.create(), listener); b.setTooltipText(tooltip); b.setAriaLabel(tooltip); b.addThemeVariants(ButtonVariant.LUMO_PRIMARY); b.addClassNames("factura-footer-boton", clase); return b; }
    private Button botonCancelar(Dialog dialogo) { Button b = new Button(VaadinIcon.CLOSE.create(), e -> dialogo.close()); b.setTooltipText("Cancelar"); b.setAriaLabel("Cancelar"); b.addClassNames("factura-footer-boton", "factura-footer-cancelar"); return b; }
    private TextField montoCampo(String etiqueta, String valor) { TextField campo = new TextField(etiqueta); campo.setValue(valor); campo.addBlurListener(e -> { try { if (!campo.getValue().isBlank()) campo.setValue(numero(FormatoMonto.parse(campo.getValue()))); } catch (Exception ignored) { } }); return campo; }
    private String fecha(LocalDate valor) { return valor == null ? "-" : valor.format(FECHA); }
    private String dinero(BigDecimal valor) { return "RD$ " + numero(valor); }
    private String numero(BigDecimal valor) { return FormatoMonto.format(valor == null ? BigDecimal.ZERO : valor); }
    private String texto(String valor) { return valor == null ? "" : valor; }
    private String estadoEcf(String valor) { return valor == null || "NO_APLICA".equals(valor) ? "No aplica" : valor.replace('_', ' '); }
    private String comprobanteFiscal(Factura factura) { String etiqueta = SecuenciaComprobanteFiscalService.etiqueta(factura.getTipoComprobante()); return factura.getNumeroComprobanteFiscal() == null ? etiqueta : etiqueta + " · " + factura.getNumeroComprobanteFiscal(); }
    private void aviso(Exception e) { Notification.show(e.getMessage() == null ? "No se pudo completar la operación." : e.getMessage(), 5000, Notification.Position.MIDDLE); }

    private record ItemFactura(String tipo, UUID id, String codigo, String nombre, BigDecimal precio, BigDecimal tasa) { }
    private record LineaEdicion(String tipo, UUID itemId, String descripcion, BigDecimal cantidad, BigDecimal precio,
                               String tipoDescuento, BigDecimal descuento, BigDecimal tasa, FacturaCalculadora.Linea calculo) {
        static LineaEdicion desde(DetalleFactura d) { UUID id = "PRODUCTO".equals(d.getTipoItem()) ? d.getProducto().getId() : d.getServicio().getId(); return new LineaEdicion(d.getTipoItem(), id, d.getDescripcion(), d.getCantidad(), d.getPrecio(), "MONTO", d.getDescuento(), d.getTasaImpuesto(), new FacturaCalculadora.Linea(d.getSubtotal(), d.getDescuento(), d.getSubtotal().subtract(d.getDescuento()), d.getImpuesto(), d.getImporte())); }
        FacturaLineaSolicitud solicitud() { return new FacturaLineaSolicitud(tipo, itemId, cantidad, precio, tipoDescuento, descuento); }
    }
}
