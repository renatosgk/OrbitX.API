package com.orbitx.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI orbitXOpenApi() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development"),
                        new Server().url("https://api.orbitx.io").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Orbit X API")
                .version("1.0.0")
                .description("""
                        **Orbit X** — Intelligent Monitoring Platform for Sustainable Datacenters.

                        Provides real-time telemetry, AI-powered predictions, orbital satellite tracking,
                        ESG reporting, and a context-aware AI assistant powered by Spring AI.

                        **Authentication**: Use `POST /api/v1/auth/login` to obtain a Bearer token,
                        then click **Authorize** and enter `Bearer <your-token>`.
                        """)
                .contact(new Contact()
                        .name("Orbit X Engineering")
                        .email("api@orbitx.io")
                        .url("https://orbitx.io"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://orbitx.io/terms"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(SECURITY_SCHEME_NAME)
                .description("Insert the JWT token obtained from POST /api/v1/auth/login");
    }
}
