package com.citacloud.app.views;

import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.EmpresaService;
import com.citacloud.app.services.NotificacionBroadcaster;
import com.citacloud.app.services.NotificacionService;
import com.citacloud.app.services.UsuarioService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    // Reserva suficiente espacio para que las vistas largas y sus paginadores no queden bajo el pie fijo.
    private static final String ESPACIO_FOOTER = "5.5rem";
    private static final String MODO_OSCURO = "citacloud.modoOscuro";
    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;
    private final NotificacionBroadcaster notificacionBroadcaster;
    private Button campana;
    private Registration suscripcionNotificaciones;

    public MainLayout(EmpresaService empresaService, UsuarioService usuarioService, NotificacionService notificacionService,
                      NotificacionBroadcaster notificacionBroadcaster) {
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
        this.notificacionBroadcaster = notificacionBroadcaster;
        aplicarModoOscuroGuardado();
        getElement().executeJs("const oscuro = localStorage.getItem('citacloud.modoOscuro') === 'true'; this.$server.sincronizarModoOscuro(oscuro);");
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        setDrawerOpened(false);
        addHeaderContent();
        addBodyFooter();
        addAttachListener(evento -> suscribirNotificaciones(evento.getUI()));
        addDetachListener(evento -> cancelarSuscripcionNotificaciones());
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        ComboBox<String> branchSelect = new ComboBox<>();
        branchSelect.setItems("Clínica San Rafael", "Sucursal Norte", "Sucursal Este");
        branchSelect.setValue("Clínica San Rafael");
        branchSelect.setPrefixComponent(VaadinIcon.BUILDING.create());
        branchSelect.setWidth("220px");

        Button searchBtn = new Button(VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button helpBtn = new Button(VaadinIcon.QUESTION_CIRCLE.create());
        helpBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        TenantUserDetails user = AuthService.getAuthenticatedUser();
        String userName = (user != null) ? user.getNombreCompleto() : "Usuario";
        String empresaNombre = user != null ? user.getEmpresaNombre() : null;
        if (empresaNombre == null || empresaNombre.isBlank()) {
            try {
                empresaNombre = user == null ? "CitaCloud" : empresaService.buscar(user.getEmpresaId()).getNombre();
            } catch (IllegalArgumentException ignored) {
                empresaNombre = "CitaCloud";
            }
        }

        RouterLink empresaActual = new RouterLink();
        empresaActual.setText(empresaNombre);
        empresaActual.getElement().setAttribute("href", "");
        empresaActual.addClassName("empresa-actual");
        empresaActual.getStyle()
                .set("font-weight", "600")
                .set("color", "#1e293b")
                .set("font-size", "1rem")
                .set("text-decoration", "none")
                .set("cursor", "pointer");

        Avatar avatar = new Avatar(userName);
        String logoEmpresa = obtenerLogoEmpresa(user);
        if (logoEmpresa != null) {
            avatar.setImage(logoEmpresa);
        }
        avatar.getStyle().set("margin-right", "0.45rem");

        MenuBar userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        var menuBtn = userMenu.addItem(avatar);
        menuBtn.add(new Span(" " + userName));
        var modoOscuro = menuBtn.getSubMenu().addItem(etiquetaModoOscuro(), e -> cambiarModoOscuro());
        modoOscuro.setId("modo-oscuro");
        boolean puedeConfigurar = user != null && user.getAuthorities().stream()
                .anyMatch(authority -> "MENU_CONFIGURACION".equals(authority.getAuthority()));
        if (puedeConfigurar) {
            menuBtn.getSubMenu().addItem("Configuración", e -> UI.getCurrent().navigate("configuracion"));
        }
        menuBtn.getSubMenu().addItem("Cerrar Sesión", e -> {
            AuthService.logout();
            UI.getCurrent().getPage().setLocation("login");
        });

        long sinLeer = user == null ? 0 : notificacionService.sinLeer(user.getEmpresaId(), user.getUsuarioId());
        campana = new Button(VaadinIcon.BELL.create());
        actualizarCampana(user, sinLeer);
        campana.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        campana.addClickListener(e -> UI.getCurrent().navigate("recordatorios"));

        HorizontalLayout headerRight = new HorizontalLayout(campana, userMenu);
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRight.setSpacing(true);

        HorizontalLayout headerLeft = new HorizontalLayout(toggle, empresaActual);
        headerLeft.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLeft.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);

        // En pantallas t\u00e1ctiles el encabezado debe mantenerse arriba, no al pie.
        addToNavbar(false, header);
    }

    private void suscribirNotificaciones(UI ui) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null) return;
        cancelarSuscripcionNotificaciones();
        suscripcionNotificaciones = notificacionBroadcaster.suscribir(usuario.getEmpresaId(), usuario.getUsuarioId(), () -> ui.access(() -> {
            long sinLeer = notificacionService.sinLeer(usuario.getEmpresaId(), usuario.getUsuarioId());
            actualizarCampana(usuario, sinLeer);
            if (sinLeer > 0) {
                Notification.show("Tienes una nueva notificación.", 3500, Notification.Position.TOP_END);
            }
        }));
    }

    private void cancelarSuscripcionNotificaciones() {
        if (suscripcionNotificaciones != null) {
            suscripcionNotificaciones.remove();
            suscripcionNotificaciones = null;
        }
    }

    private void actualizarCampana(TenantUserDetails usuario, long sinLeer) {
        if (campana == null) return;
        String descripcion = sinLeer == 0 ? "Notificaciones" : sinLeer + " notificaciones sin leer";
        campana.setTooltipText(descripcion);
        campana.setAriaLabel(descripcion);
        campana.setText(sinLeer == 0 ? "" : sinLeer > 99 ? "99+" : String.valueOf(sinLeer));
    }

    private void cambiarModoOscuro() {
        boolean oscuro = !modoOscuroActivo();
        VaadinSession.getCurrent().setAttribute(MODO_OSCURO, oscuro);
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario != null) usuarioService.guardarPreferenciaTema(usuario.getEmpresaId(), usuario.getUsuarioId(), oscuro ? "OSCURO" : "CLARO");
        aplicarTema(oscuro);
        getElement().executeJs("localStorage.setItem('citacloud.modoOscuro', $0); window.location.reload();", oscuro);
    }

    @ClientCallable
    public void sincronizarModoOscuro(boolean oscuro) {
        if (modoOscuroActivo() != oscuro) {
            VaadinSession.getCurrent().setAttribute(MODO_OSCURO, oscuro);
            aplicarTema(oscuro);
        }
    }

    private void aplicarModoOscuroGuardado() {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        boolean oscuro = modoOscuroActivo();
        if (usuario != null) {
            try {
                String preferencia = usuarioService.buscar(usuario.getEmpresaId(), usuario.getUsername(), "", null, true).stream()
                        .filter(item -> item.getId().equals(usuario.getUsuarioId())).findFirst()
                        .map(item -> item.getPreferenciaTema()).orElse("SISTEMA");
                oscuro = "OSCURO".equals(preferencia);
                VaadinSession.getCurrent().setAttribute(MODO_OSCURO, oscuro);
            } catch (Exception ignored) { }
        }
        aplicarTema(oscuro);
    }

    private boolean modoOscuroActivo() {
        Object oscuro = VaadinSession.getCurrent().getAttribute(MODO_OSCURO);
        return Boolean.TRUE.equals(oscuro);
    }

    private void aplicarTema(boolean oscuro) {
        if (oscuro) {
            UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
            UI.getCurrent().getElement().getClassList().add("modo-oscuro");
        } else {
            UI.getCurrent().getElement().getThemeList().remove(Lumo.DARK);
            UI.getCurrent().getElement().getClassList().remove("modo-oscuro");
        }
    }

    private String etiquetaModoOscuro() {
        return modoOscuroActivo() ? "Usar modo claro" : "Usar modo oscuro";
    }

    private String obtenerLogoEmpresa(TenantUserDetails usuario) {
        if (usuario != null) {
            try {
                String logoUrl = empresaService.buscar(usuario.getEmpresaId()).getLogoUrl();
                if (logoUrl != null && !logoUrl.isBlank()) {
                    return logoUrl;
                }
            } catch (IllegalArgumentException ignored) {
                // Se muestran las iniciales del usuario si no se puede recuperar el logo.
            }
        }
        return null;
    }

    private void addDrawerContent() {
        // Brand Header
        Div logoIcon = new Div(VaadinIcon.HOSPITAL.create());
        logoIcon.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("background-color", "#1565D8")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "1.25rem");

        H2 brandTitle = new H2("CitaCloud");
        brandTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("font-weight", "800")
                .set("color", "#1565D8");

        Span brandSub = new Span("Gestión de citas");
        brandSub.getStyle()
                .set("font-size", "0.6875rem")
                .set("color", "#64748b")
                .set("display", "block");

        Header header = new Header(new HorizontalLayout(logoIcon, new Div(brandTitle, brandSub)));
        header.addClassNames(LumoUtility.Padding.MEDIUM);

        SideNav navigation = createNavigation();
        navigation.addClassNames(LumoUtility.Padding.SMALL);
        navigation.getElement().addEventListener("click", event -> setDrawerOpened(false));
        // AppLayout ya controla el desplazamiento del drawer; un Scroller aquí
        // producía una segunda barra vertical.
        addToDrawer(header, navigation);
    }

    private void addBodyFooter() {
        Footer footer = new Footer(new Span("\u00a9 " + java.time.Year.now().getValue() + " CitaCloud · Gestión de citas"));
        footer.addClassName("citacloud-footer");
        footer.getStyle()
                .set("position", "fixed")
                .set("left", "var(--vaadin-app-layout-drawer-width, 0px)")
                .set("right", "0")
                .set("bottom", "0")
                .set("min-height", "2.75rem")
                // El contenido reserva altura inferior antes de este pie fijo.
                .set("z-index", "10")
                .set("padding", "0.55rem 1.5rem")
                .set("background-color", "#ffffff")
                .set("color", "#64748b")
                .set("font-size", "0.75rem")
                .set("border-top", "1px solid #e2e8f0")
                .set("text-align", "center");
        getElement().appendChild(footer.getElement());
    }

    /**
     * El pie de p\u00e1gina permanece fijo en el cuerpo de la aplicaci\u00f3n. Reservamos
     * su altura en cada vista para que nunca cubra tablas, formularios ni acciones.
     */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        content.getElement().getStyle()
                .set("padding-bottom", ESPACIO_FOOTER)
                .set("margin-bottom", ESPACIO_FOOTER)
                .set("box-sizing", "border-box");
        super.showRouterLayoutContent(content);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", "", VaadinIcon.DASHBOARD.create()));
        SideNavItem gestionClinica = new SideNavItem("GESTIÓN CLÍNICA");
        gestionClinica.addClassName("sidebar-section");
        nav.addItem(gestionClinica);
        nav.addItem(new SideNavItem("Mi agenda", "mi-agenda", VaadinIcon.CALENDAR_CLOCK.create()));
        nav.addItem(new SideNavItem("Citas", "citas", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Pacientes", "pacientes", VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Médicos", "medicos", VaadinIcon.DOCTOR.create()));
        nav.addItem(new SideNavItem("Especialidades", "especialidades", VaadinIcon.DIPLOMA.create()));
        nav.addItem(new SideNavItem("Horarios", "horarios", VaadinIcon.CLOCK.create()));
        nav.addItem(new SideNavItem("Consultorios", "consultorios", VaadinIcon.OFFICE.create()));
        nav.addItem(new SideNavItem("Seguros", "seguros", VaadinIcon.SHIELD.create()));
        nav.addItem(new SideNavItem("Servicios", "servicios", VaadinIcon.STETHOSCOPE.create()));
        SideNavItem administracion = new SideNavItem("ADMINISTRACIÓN");
        administracion.addClassName("sidebar-section");
        nav.addItem(administracion);
        nav.addItem(new SideNavItem("Usuarios", "usuarios", VaadinIcon.USER_CHECK.create()));
        nav.addItem(new SideNavItem("Roles", "roles", VaadinIcon.KEY.create()));
        SideNavItem operacionClinica = new SideNavItem("OPERACIÓN CLÍNICA");
        operacionClinica.addClassName("sidebar-section");
        nav.addItem(operacionClinica);
        nav.addItem(new SideNavItem("Recepción", "recepcion", VaadinIcon.DESKTOP.create()));
        nav.addItem(new SideNavItem("Consulta médica", "consulta-medica", VaadinIcon.STETHOSCOPE.create()));
        nav.addItem(new SideNavItem("Expediente clínico", "historial-clinico", VaadinIcon.CLIPBOARD_HEART.create()));
        nav.addItem(new SideNavItem("Notificaciones", "recordatorios", VaadinIcon.BELL.create()));
        nav.addItem(new SideNavItem("Documentos", "documentos", VaadinIcon.FILE_TEXT.create()));
        SideNavItem gestionFinanciera = new SideNavItem("GESTIÓN FINANCIERA Y OPERATIVA");
        gestionFinanciera.addClassName("sidebar-section");
        nav.addItem(gestionFinanciera);
        nav.addItem(new SideNavItem("Facturación", "facturacion", VaadinIcon.INVOICE.create()));
        nav.addItem(new SideNavItem("e-CF", "e-cf", VaadinIcon.FILE_CODE.create()));
        nav.addItem(new SideNavItem("Cierre Caja", "caja", VaadinIcon.CASH.create()));
        TenantUserDetails usuarioActual = AuthService.getAuthenticatedUser();
        if (usuarioActual != null && usuarioActual.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_SUPERADMIN") || a.getAuthority().equals("CASH_REGISTER_VIEW"))) {
            nav.addItem(new SideNavItem("Cajas", "cajas", VaadinIcon.CASH.create()));
        }
        nav.addItem(new SideNavItem("Pagos", "pagos", VaadinIcon.CREDIT_CARD.create()));
        nav.addItem(new SideNavItem("Cuentas por cobrar", "cuentas-por-cobrar", VaadinIcon.DOLLAR.create()));
        nav.addItem(new SideNavItem("Inventario", "inventario", VaadinIcon.PACKAGE.create()));
        nav.addItem(new SideNavItem("Laboratorio", "laboratorio", VaadinIcon.FLASK.create()));
        nav.addItem(new SideNavItem("Reportes financieros", "reportes-financieros", VaadinIcon.CHART_LINE.create()));
        SideNavItem gestionControl = new SideNavItem("GESTIÓN Y CONTROL");
        gestionControl.addClassName("sidebar-section");
        nav.addItem(gestionControl);
        nav.addItem(new SideNavItem("Gestión y Control", "reportes/gestion-control", VaadinIcon.TRENDING_UP.create()));
        nav.addItem(new SideNavItem("Auditoría", "auditoria", VaadinIcon.EYE.create()));
        SideNavItem empresas = new SideNavItem("Empresas", "empresas", VaadinIcon.BUILDING.create());
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        // No se agrega al árbol de navegación para perfiles normales; no basta ocultarlo visualmente.
        if (usuario != null && "SUPERADMIN".equalsIgnoreCase(usuario.getEmpresaCodigo())
                && usuario.getAuthorities().stream().anyMatch(authority -> "ROLE_SUPERADMIN".equals(authority.getAuthority()))) {
            nav.addItem(empresas);
        }
        aplicarPermisosMenu(nav);
        return nav;
    }

    private void aplicarPermisosMenu(SideNav nav) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null) {
            return;
        }
        boolean esAdministrador = usuario.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRADOR".equals(authority.getAuthority())
                        || "ROLE_SUPERADMIN".equals(authority.getAuthority()));
        String[] permisos = {"MENU_DASHBOARD", null, "MENU_MI_AGENDA", "MENU_CITAS", "MENU_PACIENTES", "MENU_MEDICOS",
                "MENU_ESPECIALIDADES", "MENU_HORARIOS", "MENU_CONSULTORIOS", "MENU_SEGUROS", "MENU_SERVICIOS", null,
                "MENU_USUARIOS", "MENU_ROLES", null, null, "MENU_SIGNOS_VITALES", "MENU_CONSULTA_MEDICA", "MENU_EXPEDIENTE_CLINICO", "MENU_ANTECEDENTES", "MENU_ALERGIAS", "MENU_DIAGNOSTICOS", "MENU_TRATAMIENTOS", "MENU_RECETAS", "MENU_ORDENES_ESTUDIOS", "MENU_RECORDATORIOS",
                "MENU_DOCUMENTOS", null, "MENU_FACTURACION", "MENU_ECF", "MENU_CAJA", "MENU_PAGOS", "MENU_CUENTAS_COBRAR", "MENU_INVENTARIO", "MENU_LABORATORIO", "MENU_REPORTES_FINANCIEROS", null, "MENU_REPORTES", "MENU_GESTION_CONTROL", "MENU_AUDITORIA", null};
        var elementos = nav.getElement().getChildren().toList();
        boolean puedeAccederRecepcion = esAdministrador || usuario.getAuthorities().stream()
                .anyMatch(authority -> "MENU_APROBACION_CITAS".equals(authority.getAuthority())
                        || "MENU_CHECKIN".equals(authority.getAuthority())
                        || "MENU_SALA_ESPERA".equals(authority.getAuthority()));
        for (int indice = 0; indice < elementos.size() && indice < permisos.length; indice++) {
            String permiso = permisos[indice];
            if (permiso == null) {
                // Recepción agrupa los tres permisos operativos que antes tenían menú propio.
                if (indice == 14) elementos.get(indice).setVisible(puedeAccederRecepcion);
                continue;
            }
            boolean permitido = esAdministrador || usuario.getAuthorities().stream()
                    .anyMatch(authority -> permiso.equals(authority.getAuthority()));
            elementos.get(indice).setVisible(permitido);
        }
    }
}
