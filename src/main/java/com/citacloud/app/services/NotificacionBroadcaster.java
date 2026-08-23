package com.citacloud.app.services;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Mantiene actualizados los clientes Vaadin conectados a una misma empresa. */
@Component
public class NotificacionBroadcaster {
    private final Set<Suscriptor> suscriptores = ConcurrentHashMap.newKeySet();

    public Registration suscribir(UUID empresaId, Runnable accion) {
        Suscriptor suscriptor = new Suscriptor(empresaId, accion);
        suscriptores.add(suscriptor);
        return () -> suscriptores.remove(suscriptor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notificarDespuesDeConfirmar(NotificacionesCreadasEvent evento) {
        publicarEmpresa(evento.empresaId());
    }

    private void publicarEmpresa(UUID empresaId) {
        suscriptores.stream().filter(suscriptor -> empresaId.equals(suscriptor.empresaId()))
                .forEach(Suscriptor::actualizar);
    }

    private record Suscriptor(UUID empresaId, Runnable accion) {
        void actualizar() {
            accion.run();
        }
    }
}
