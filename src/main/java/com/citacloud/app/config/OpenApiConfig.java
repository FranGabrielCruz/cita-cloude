package com.citacloud.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos y esquema de autenticación de la documentación HTTP de Cita Cloud. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI citaCloudOpenApi() {
        String sessionCookie = "sessionCookie";
        return new OpenAPI()
                .info(new Info()
                        .title("Cita Cloud API")
                        .version("0.0.1")
                        .description("Contrato HTTP generado desde los controladores implementados."))
                .components(new Components().addSecuritySchemes(sessionCookie,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Sesión autenticada de Cita Cloud.")))
                .addSecurityItem(new SecurityRequirement().addList(sessionCookie));
    }
}
