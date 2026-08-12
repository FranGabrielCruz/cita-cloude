package com.citacloud.app.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Cierra la sesión tras quince minutos sin interacción del usuario. */
@Component
public class InactivityLogoutListener implements VaadinServiceInitListener {

    private static final int INACTIVIDAD_MS = 15 * 60 * 1000;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().getPage().executeJs("""
                (() => {
                  const timeout = $0;
                  let temporizador;
                  const cerrarPorInactividad = () => window.location.assign('logout');
                  const registrarActividad = () => {
                    window.clearTimeout(temporizador);
                    temporizador = window.setTimeout(cerrarPorInactividad, timeout);
                  };
                  ['click', 'keydown', 'pointerdown', 'touchstart', 'input', 'change', 'scroll']
                    .forEach(evento => window.addEventListener(evento, registrarActividad, { passive: true }));
                  registrarActividad();
                })();
                """, INACTIVIDAD_MS));
    }
}
