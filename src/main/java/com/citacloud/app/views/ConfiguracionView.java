package com.citacloud.app.views;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.models.ConfiguracionEmpresaFase2;
import com.citacloud.app.models.SecuenciaComprobanteFiscal;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EmpresaService;
import com.citacloud.app.services.SucursalService;
import com.citacloud.app.services.ConfiguracionFase2Service;
import com.citacloud.app.services.SecuenciaComprobanteFiscalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.UUID;

@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuraci\u00f3n | CitaCloud")
@PermitAll
public class ConfiguracionView extends VerticalLayout {

    private final EmpresaService empresaService;
    private final SucursalService sucursalService;
    private final ConfiguracionFase2Service configuracionFase2Service;
    private final SecuenciaComprobanteFiscalService secuenciasFiscales;
    private final UUID empresaId;
    private final Grid<Sucursal> sucursales = new Grid<>(Sucursal.class, false);

    public ConfiguracionView(EmpresaService empresaService, SucursalService sucursalService,
                             ConfiguracionFase2Service configuracionFase2Service,
                             SecuenciaComprobanteFiscalService secuenciasFiscales) {
        this.empresaService = empresaService;
        this.sucursalService = sucursalService;
        this.configuracionFase2Service = configuracionFase2Service;
        this.secuenciasFiscales = secuenciasFiscales;
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        empresaId = usuario == null ? null : usuario.getEmpresaId();

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        H2 titulo = new H2("Configuraci\u00f3n");
        titulo.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "800");
        DatosInstitucion datosInstitucion = crearDatosInstitucion();
        HorizontalLayout encabezado = new HorizontalLayout(titulo, datosInstitucion.guardar());
        encabezado.setWidthFull(); encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        add(encabezado, datosInstitucion.tarjeta(), crearPreferenciasOperativas(), crearSecuenciasFiscales(), crearSucursales());
    }

    private VerticalLayout crearSecuenciasFiscales() {
        VerticalLayout tarjeta = tarjeta();
        H3 titulo = new H3("Secuencias de comprobantes fiscales"); titulo.getStyle().set("margin", "0");
        Span ayuda = new Span("Configura el rango desde/hasta que utilizará cada tipo de comprobante al emitir una factura.");
        ayuda.getStyle().set("color", "#64748b");
        Grid<SecuenciaComprobanteFiscal> tabla = new Grid<>(SecuenciaComprobanteFiscal.class, false);
        tabla.addColumn(SecuenciaComprobanteFiscal::getNombre).setHeader("TIPO").setFlexGrow(1);
        tabla.addColumn(SecuenciaComprobanteFiscal::getPrefijo).setHeader("PREFIJO").setAutoWidth(true);
        tabla.addColumn(SecuenciaComprobanteFiscal::getNumeroDesde).setHeader("DESDE").setAutoWidth(true);
        tabla.addColumn(SecuenciaComprobanteFiscal::getNumeroHasta).setHeader("HASTA").setAutoWidth(true);
        tabla.addColumn(SecuenciaComprobanteFiscal::getNumeroSiguiente).setHeader("SIGUIENTE").setAutoWidth(true);
        tabla.addComponentColumn(s -> { Span estado = new Span(Boolean.TRUE.equals(s.getActiva()) ? "Activa" : "Inactiva"); estado.addClassName(Boolean.TRUE.equals(s.getActiva()) ? "badge-activo" : "badge-inactivo"); return estado; }).setHeader("ESTADO").setAutoWidth(true);
        tabla.addComponentColumn(s -> { Button editar = botonIcono(VaadinIcon.EDIT.create(), "Editar secuencia", "transparent", "#2563eb"); editar.addClickListener(e -> abrirSecuenciaFiscal(tabla, s)); return editar; }).setHeader("ACCIONES").setAutoWidth(true);
        tabla.setAllRowsVisible(true); tabla.setWidthFull();
        Runnable cargar = () -> tabla.setItems(empresaId == null ? List.of() : secuenciasFiscales.listar(empresaId));
        tabla.getElement().setProperty("cargarSecuencias", "true"); cargar.run();
        Button nueva = botonIcono(VaadinIcon.PLUS.create(), "Nueva secuencia fiscal", "#16a34a", "white");
        nueva.addClickListener(e -> abrirSecuenciaFiscal(tabla, null));
        HorizontalLayout encabezado = new HorizontalLayout(new VerticalLayout(titulo, ayuda), nueva); encabezado.setWidthFull(); encabezado.setAlignItems(FlexComponent.Alignment.CENTER); encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); encabezado.getComponentAt(0).getElement().getStyle().set("padding", "0");
        tarjeta.add(encabezado, tabla);
        return tarjeta;
    }

    private void abrirSecuenciaFiscal(Grid<SecuenciaComprobanteFiscal> tabla, SecuenciaComprobanteFiscal existente) {
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle(existente == null ? "Nueva secuencia fiscal" : "Editar secuencia fiscal"); dialogo.setWidth("min(680px,95vw)");
        ComboBox<String> tipo = new ComboBox<>("Tipo/comprobante fiscal *"); tipo.setItems(SecuenciaComprobanteFiscalService.TIPOS.keySet()); tipo.setItemLabelGenerator(SecuenciaComprobanteFiscalService::etiqueta);
        TextField prefijo = new TextField("Prefijo *"); prefijo.setHelperText("Ejemplo: B01 o E31");
        TextField desde = new TextField("Secuencia desde *"); TextField hasta = new TextField("Secuencia hasta *");
        Checkbox activa = new Checkbox("Secuencia activa", true);
        if (existente != null) { tipo.setValue(existente.getTipo()); prefijo.setValue(valor(existente.getPrefijo())); desde.setValue(String.valueOf(existente.getNumeroDesde())); hasta.setValue(String.valueOf(existente.getNumeroHasta())); activa.setValue(Boolean.TRUE.equals(existente.getActiva())); }
        FormLayout formulario = new FormLayout(tipo, prefijo, desde, hasta, activa); formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("560px", 2));
        Button guardar = botonIcono(VaadinIcon.DISC.create(), "Guardar secuencia", "#16a34a", "white");
        guardar.addClickListener(e -> { try { secuenciasFiscales.guardar(empresaId, existente == null ? null : existente.getId(), tipo.getValue(), prefijo.getValue(), Long.parseLong(desde.getValue().trim()), Long.parseLong(hasta.getValue().trim()), activa.getValue()); tabla.setItems(secuenciasFiscales.listar(empresaId)); dialogo.close(); Notification.show("Secuencia fiscal guardada.", 3000, Notification.Position.BOTTOM_START); } catch (NumberFormatException ex) { Notification.show("Las secuencias desde y hasta deben contener solo números enteros.", 4000, Notification.Position.MIDDLE); } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE); } });
        Button cerrar = botonIcono(VaadinIcon.CLOSE.create(), "Cerrar", "#e2e8f0", "#1e293b"); cerrar.addClickListener(e -> dialogo.close());
        dialogo.add(formulario); dialogo.getFooter().add(guardar, cerrar); dialogo.open();
    }

    private VerticalLayout crearPreferenciasOperativas() {
        VerticalLayout tarjeta = tarjeta();
        H3 titulo = new H3("Preferencias operativas");
        titulo.getStyle().set("margin", "0");
        ConfiguracionEmpresaFase2 configuracion = empresaId == null
                ? new ConfiguracionEmpresaFase2() : configuracionFase2Service.obtener(empresaId);
        Checkbox requiereAprobacion = new Checkbox("Las solicitudes de pacientes requieren aprobación",
                configuracion.isRequiereAprobacionCitas());
        Checkbox recordatorios = new Checkbox("Recordatorios activos", configuracion.isRecordatoriosActivos());
        Checkbox notificaciones = new Checkbox("Notificaciones activas", configuracion.isNotificacionesActivas());
        TextField prefijo = new TextField("Prefijo de factura", valor(configuracion.getPrefijoFactura()));
        IntegerField siguiente = new IntegerField("Siguiente número de factura");
        siguiente.setValue(configuracion.getSiguienteFactura() > 0 ? configuracion.getSiguienteFactura() : 1);
        prefijo.setRequiredIndicatorVisible(true);
        siguiente.setRequiredIndicatorVisible(true);
        ComboBox<String> unidadAltura = new ComboBox<>("Unidad de altura", List.of("METROS", "PIES")); unidadAltura.setValue(configuracion.getUnidadAltura());
        ComboBox<String> unidadPeso = new ComboBox<>("Unidad de peso", List.of("KG", "LIBRAS")); unidadPeso.setValue(configuracion.getUnidadPeso());
        ComboBox<String> unidadTemperatura = new ComboBox<>("Unidad de temperatura", List.of("C", "F")); unidadTemperatura.setValue(configuracion.getUnidadTemperatura());
        FormLayout formulario = new FormLayout(requiereAprobacion, recordatorios, notificaciones, prefijo, siguiente, unidadAltura, unidadPeso, unidadTemperatura);
        formulario.setWidthFull();
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("620px", 2));
        Button guardar = new Button(VaadinIcon.DISC.create());
        guardar.setTooltipText("Guardar preferencias");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("background-color", "#16a34a").set("color", "#ffffff");
        guardar.addClickListener(event -> {
            try {
                if (empresaId == null) throw new IllegalArgumentException("No se pudo identificar la empresa.");
                configuracionFase2Service.guardar(empresaId, requiereAprobacion.getValue(), prefijo.getValue(),
                        siguiente.getValue() == null ? 1 : siguiente.getValue(), recordatorios.getValue(), notificaciones.getValue(), unidadAltura.getValue(), unidadPeso.getValue(), unidadTemperatura.getValue());
                Notification.show("Preferencias guardadas.", 3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException exception) {
                Notification.show(exception.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        HorizontalLayout encabezado = new HorizontalLayout(titulo, guardar);
        encabezado.setWidthFull();
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        tarjeta.add(encabezado, formulario);
        return tarjeta;
    }

    private DatosInstitucion crearDatosInstitucion() {
        VerticalLayout tarjeta = tarjeta();
        H3 subtitulo = new H3("Datos de la Instituci\u00f3n");
        subtitulo.getStyle().set("margin", "0");
        TextField nombre = new TextField("Nombre de la Instituci\u00f3n");
        TextField rnc = new TextField("RNC / Identificaci\u00f3n");
        TextField telefono = new TextField("Tel\u00e9fono principal");
        TextField correo = new TextField("Correo electr\u00f3nico");
        TextArea direccion = new TextArea("Direcci\u00f3n");
        direccion.setWidthFull();
        FormLayout formulario = new FormLayout(nombre, rnc, telefono, correo, direccion);
        formulario.setWidthFull();
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("650px", 2));
        formulario.setColspan(nombre, 2);
        formulario.setColspan(direccion, 2);

        Empresa empresaActual = null;
        if (empresaId != null) {
            try {
                empresaActual = empresaService.buscar(empresaId);
                nombre.setValue(valor(empresaActual.getNombre())); rnc.setValue(valor(empresaActual.getRncIdentificacion()));
                telefono.setValue(valor(empresaActual.getTelefono())); correo.setValue(valor(empresaActual.getEmail())); direccion.setValue(valor(empresaActual.getDireccion()));
            } catch (IllegalArgumentException ignored) { }
        }
        Button guardar = botonIcono(VaadinIcon.DISC.create(), "Guardar", "#16a34a", "white");
        guardar.addClickListener(event -> {
            try {
                if (empresaId == null) throw new IllegalArgumentException("No se pudo identificar la instituci\u00f3n.");
                empresaService.guardar(empresaId, nombre.getValue(), rnc.getValue(), telefono.getValue(), correo.getValue(), direccion.getValue());
                Notification.show("Datos guardados.", 3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE); }
        });
        tarjeta.add(subtitulo, formulario, crearCargaLogo(empresaActual));
        return new DatosInstitucion(tarjeta, guardar);
    }

    private VerticalLayout crearCargaLogo(Empresa empresa) {
        VerticalLayout seccion = new VerticalLayout();
        seccion.setPadding(false); seccion.setSpacing(false);
        seccion.getStyle().set("margin-top", "0.5rem");
        H3 titulo = new H3("Logo de la institución");
        titulo.getStyle().set("margin", "0 0 0.5rem 0").set("font-size", "1rem");

        if (empresa != null && empresa.getLogoUrl() != null && !empresa.getLogoUrl().isBlank()) {
            Image vistaPrevia = new Image(empresa.getLogoUrl(), "Logo de " + empresa.getNombre());
            vistaPrevia.setWidth("96px"); vistaPrevia.setHeight("96px");
            vistaPrevia.getStyle().set("object-fit", "contain").set("border", "1px solid #e2e8f0").set("border-radius", "10px").set("padding", "0.35rem");
            seccion.add(titulo, vistaPrevia);
        } else {
            seccion.add(titulo);
        }

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload carga = new Upload(buffer);
        carga.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
        carga.setMaxFiles(1); carga.setMaxFileSize(2 * 1024 * 1024);
        carga.setUploadButton(new Button("Seleccionar logo", VaadinIcon.UPLOAD.create()));
        carga.setDropLabel(new Span("PNG, JPG o WEBP; máximo 2 MB"));
        carga.setWidthFull();
        carga.addSucceededListener(event -> {
            try (var contenido = buffer.getInputStream(event.getFileName())) {
                if (empresaId == null) throw new IllegalArgumentException("No se pudo identificar la institución.");
                empresaService.guardarLogo(empresaId, contenido, event.getFileName(), event.getMIMEType());
                Notification.show("Logo actualizado.", 3000, Notification.Position.BOTTOM_START);
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (Exception ex) {
                String mensaje = ex instanceof IllegalArgumentException ? ex.getMessage() : "No fue posible guardar el logo.";
                Notification.show(mensaje, 4000, Notification.Position.MIDDLE);
            }
        });
        carga.addFileRejectedListener(event -> Notification.show("El logo debe ser una imagen de hasta 2 MB.", 4000, Notification.Position.MIDDLE));
        seccion.add(carga);
        return seccion;
    }

    private VerticalLayout crearSucursales() {
        VerticalLayout tarjeta = tarjeta();
        H3 subtitulo = new H3("Sucursales");
        subtitulo.getStyle().set("margin", "0");
        Button nueva = botonIcono(VaadinIcon.PLUS.create(), "Nueva sucursal", "#16a34a", "white");
        nueva.addClickListener(event -> abrirSucursal(null));
        HorizontalLayout cabecera = new HorizontalLayout(subtitulo, nueva);
        cabecera.setWidthFull(); cabecera.setAlignItems(FlexComponent.Alignment.CENTER);
        cabecera.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        sucursales.addColumn(Sucursal::getCodigo).setHeader("C\u00d3DIGO").setWidth("130px").setFlexGrow(0);
        sucursales.addColumn(Sucursal::getNombre).setHeader("NOMBRE");
        sucursales.addColumn(Sucursal::getTelefono).setHeader("TEL\u00c9FONO");
        sucursales.addComponentColumn(sucursal -> {
            Span estado = new Span(Boolean.TRUE.equals(sucursal.getActiva()) ? "Activa" : "Inactiva");
            estado.addClassName(Boolean.TRUE.equals(sucursal.getActiva()) ? "badge-activo" : "badge-inactivo");
            return estado;
        }).setHeader("ESTADO").setWidth("130px").setFlexGrow(0);
        sucursales.addComponentColumn(sucursal -> {
            Button editar = new Button(VaadinIcon.EDIT.create(), event -> abrirSucursal(sucursal));
            editar.setTooltipText("Editar"); editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return editar;
        }).setHeader("ACCIONES").setWidth("110px").setFlexGrow(0);
        sucursales.setWidthFull(); sucursales.setAllRowsVisible(true);
        cargarSucursales();
        tarjeta.add(cabecera, sucursales);
        return tarjeta;
    }

    private void abrirSucursal(Sucursal existente) {
        if (empresaId == null) { Notification.show("No se pudo identificar la instituci\u00f3n."); return; }
        boolean edicion = existente != null;
        Dialog dialogo = new Dialog(); dialogo.setHeaderTitle(edicion ? "Editar sucursal" : "Nueva sucursal"); dialogo.setWidth("min(680px, 95vw)");
        TextField codigo = new TextField("C\u00f3digo"); TextField nombre = new TextField("Nombre"); TextField telefono = new TextField("Tel\u00e9fono"); TextField correo = new TextField("Correo electr\u00f3nico"); TextArea direccion = new TextArea("Direcci\u00f3n"); direccion.setWidthFull();
        if (edicion) { codigo.setValue(valor(existente.getCodigo())); nombre.setValue(valor(existente.getNombre())); telefono.setValue(valor(existente.getTelefono())); correo.setValue(valor(existente.getEmail())); direccion.setValue(valor(existente.getDireccion())); }
        FormLayout formulario = new FormLayout(codigo, nombre, telefono, correo, direccion); formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2)); formulario.setColspan(direccion, 2); formulario.setWidthFull();
        Button guardar = botonIcono(VaadinIcon.DISC.create(), "Guardar", "#16a34a", "white");
        guardar.addClickListener(event -> {
            try {
                if (codigo.isEmpty() || nombre.isEmpty()) throw new IllegalArgumentException("C\u00f3digo y nombre son obligatorios.");
                Sucursal sucursal = edicion ? existente : new Sucursal();
                sucursal.setEmpresaId(empresaId); sucursal.setCodigo(codigo.getValue().trim().toUpperCase()); sucursal.setNombre(nombre.getValue().trim()); sucursal.setTelefono(limpiar(telefono.getValue())); sucursal.setEmail(limpiar(correo.getValue())); sucursal.setDireccion(limpiar(direccion.getValue())); if (!edicion) sucursal.setActiva(true);
                sucursalService.guardar(sucursal); cargarSucursales(); dialogo.close(); Notification.show("Sucursal guardada.", 3000, Notification.Position.BOTTOM_START);
            } catch (IllegalArgumentException ex) { Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE); }
        });
        Button cambiarEstado = null;
        if (edicion) { boolean activa = Boolean.TRUE.equals(existente.getActiva()); cambiarEstado = botonIcono(activa ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(), activa ? "Desactivar" : "Activar", activa ? "#dc2626" : "#16a34a", "white"); cambiarEstado.addClickListener(event -> { existente.setActiva(!activa); sucursalService.guardar(existente); cargarSucursales(); dialogo.close(); Notification.show(activa ? "Sucursal desactivada." : "Sucursal activada.", 3000, Notification.Position.BOTTOM_START); }); }
        Button cerrar = botonIcono(VaadinIcon.CLOSE.create(), "Cerrar", "#e2e8f0", "#1e293b"); cerrar.addClickListener(event -> dialogo.close());
        HorizontalLayout acciones = cambiarEstado == null ? new HorizontalLayout(guardar, cerrar) : new HorizontalLayout(guardar, cambiarEstado, cerrar);
        dialogo.add(formulario); dialogo.getFooter().add(acciones); dialogo.open();
    }

    private void cargarSucursales() { sucursales.setItems(empresaId == null ? List.of() : sucursalService.listarPorEmpresa(empresaId)); }
    private VerticalLayout tarjeta() { VerticalLayout tarjeta = new VerticalLayout(); tarjeta.setWidthFull(); tarjeta.getStyle().set("background-color", "#ffffff").set("border-radius", "12px").set("border", "1px solid #e2e8f0").set("padding", "1.5rem"); return tarjeta; }
    private Button botonIcono(com.vaadin.flow.component.Component icono, String tooltip, String fondo, String color) { Button boton = new Button(icono); boton.setTooltipText(tooltip); boton.getStyle().set("background", fondo).set("color", color); return boton; }
    private String valor(String texto) { return texto == null ? "" : texto; }
    private String limpiar(String texto) { return texto == null || texto.isBlank() ? null : texto.trim(); }
    private record DatosInstitucion(VerticalLayout tarjeta, Button guardar) { }
}
