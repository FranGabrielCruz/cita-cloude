package com.citacloud.app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SwaggerUiControllerTest {

    private final SwaggerUiController controller = new SwaggerUiController();

    @Test
    void sirveSwaggerSinDependerDelEnrutadorDeVaadin() {
        String html = controller.swaggerUi();

        assertTrue(html.contains("id=\"swagger-ui\""));
        assertTrue(html.contains("url: '/v3/api-docs'"));
        assertTrue(html.contains("/webjars/swagger-ui/5.17.14/swagger-ui-bundle.js"));
    }
}
