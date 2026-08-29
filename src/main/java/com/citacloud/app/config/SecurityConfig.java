package com.citacloud.app.config;

import com.citacloud.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final ApiSecurityErrorWriter apiSecurityErrorWriter;

    public SecurityConfig(ApiSecurityErrorWriter apiSecurityErrorWriter) {
        this.apiSecurityErrorWriter = apiSecurityErrorWriter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**", "/line-awesome/**", "/uploads/**").permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?expirada")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .securityContext(context -> context.requireExplicitSave(false));

        // El visor de recetas se carga en un iframe de la misma aplicación.
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        super.configure(http);
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        apiSecurityErrorWriter.write(request, response, org.springframework.http.HttpStatus.UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        apiSecurityErrorWriter.write(request, response, org.springframework.http.HttpStatus.FORBIDDEN);
                    } else {
                        response.sendError(org.springframework.http.HttpStatus.FORBIDDEN.value());
                    }
                }));
        setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
