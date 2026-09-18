package com.citacloud.app.controllers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Sirve Swagger UI explícitamente para evitar que el fallback de rutas de
 * Vaadin capture {@code /swagger-ui/index.html}.
 */
@Controller
public class SwaggerUiController {

    private static final String SWAGGER_UI_VERSION = "5.17.14";

    @GetMapping(value = {"/swagger-ui.html", "/swagger-ui", "/swagger-ui/index.html"},
            produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String swaggerUi() {
        String base = "/webjars/swagger-ui/" + SWAGGER_UI_VERSION;
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Cita Cloud API</title>
                  <link rel="stylesheet" href="%s/swagger-ui.css">
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="%s/swagger-ui-bundle.js"></script>
                  <script src="%s/swagger-ui-standalone-preset.js"></script>
                  <script>
                    window.onload = () => SwaggerUIBundle({
                      url: '/v3/api-docs',
                      dom_id: '#swagger-ui',
                      deepLinking: true,
                      tryItOutEnabled: false,
                      operationsSorter: 'method',
                      tagsSorter: 'alpha',
                      presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
                      layout: 'StandaloneLayout'
                    });
                  </script>
                </body>
                </html>
                """.formatted(base, base, base);
    }
}
