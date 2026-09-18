package com.citacloud.app.services;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PlantillaNotificacionServiceTest {
    private final PlantillaNotificacionService servicio = new PlantillaNotificacionService();

    @Test void renderizaTodasLasVariablesControladas() {
        String plantilla = "{{paciente}}|{{fecha_cita}}|{{hora_cita}}|{{medico}}|{{especialidad}}|{{sucursal}}|{{consultorio}}|{{clinica}}";
        Map<String,String> ejemplo = servicio.ejemplo();
        String resultado = servicio.renderizar(plantilla, ejemplo);
        assertFalse(resultado.contains("{{"));
        ejemplo.values().forEach(valor -> assertTrue(resultado.contains(valor)));
    }

    @Test void rechazaVariableDesconocida() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.validar("Hola {{nombre_del_perro}}"));
        assertEquals("La variable {{nombre_del_perro}} no es válida.", error.getMessage());
    }

    @Test void rechazaVariableIncompleta() {
        assertThrows(IllegalArgumentException.class, () -> servicio.validar("Hola {{paciente}"));
    }
}
