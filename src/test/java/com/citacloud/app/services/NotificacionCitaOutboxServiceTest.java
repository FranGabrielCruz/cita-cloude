package com.citacloud.app.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificacionCitaOutboxServiceTest {
    @Test void normalizacionInternacionalNoInventaNumerosInvalidos() {
        NotificacionCitaOutboxService servicio = new NotificacionCitaOutboxService(null,null,null,null,null);
        assertEquals("+18095550100", servicio.normalizarTelefono("(809) 555-0100"));
        assertEquals("+18095550100", servicio.normalizarTelefono("+1 809 555 0100"));
        assertNull(servicio.normalizarTelefono("555"));
        assertNull(servicio.normalizarTelefono(null));
    }
}
