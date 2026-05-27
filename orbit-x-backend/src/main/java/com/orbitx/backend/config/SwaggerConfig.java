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
                        new Server().url("http://localhost:" + serverPort).description("Desenvolvimento Local"),
                        new Server().url("https://api.orbitx.io").description("Produção")
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
                        **Orbit X** — Plataforma de Monitoramento Inteligente para Datacenters Sustentáveis.

                        Fornece telemetria em tempo real, predições baseadas em IA, rastreamento de satélites orbitais,
                        relatórios ESG e um assistente de IA contextual desenvolvido com Spring AI.

                        **Autenticação**: Use `POST /api/v1/auth/login` para obter um token Bearer,
                        depois clique em **Authorize** e informe `Bearer <seu-token>`.
                        """)
                .contact(new Contact()
                        .name("Orbit X Engenharia")
                        .email("api@orbitx.io")
                        .url("https://orbitx.io"))
                .license(new License()
                        .name("Proprietário")
                        .url("https://orbitx.io/termos"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(SECURITY_SCHEME_NAME)
                .description("Informe o token JWT obtido em POST /api/v1/auth/login");
    }
}
