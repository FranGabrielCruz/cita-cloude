package com.citacloud.app.config;

import com.citacloud.app.models.Caja;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.core.converter.ModelConverters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void publicaMetadatosYAutenticacionPorSesion() {
        OpenAPI openAPI = new OpenApiConfig().citaCloudOpenApi();

        assertEquals("Cita Cloud API", openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("sessionCookie"));
        assertEquals("JSESSIONID",
                openAPI.getComponents().getSecuritySchemes().get("sessionCookie").getName());
    }

    @Test
    void generaEsquemasJakartaSinColisionConSwaggerDeHilla() {
        assertNotNull(ModelConverters.getInstance().readAll(Caja.class).get("Caja"));
    }
}
