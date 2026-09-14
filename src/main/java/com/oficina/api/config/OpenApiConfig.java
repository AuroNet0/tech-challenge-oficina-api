package com.oficina.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("API Oficina Mecânica")
                        .description("API REST para gestão de clientes, veículos, ordens de serviço, serviços, peças, insumos, estoque e autenticação JWT.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Auro Neto")
                                .email("auroneto04@gmail.com")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Informe o token JWT no formato: Bearer {token}")
                        )
                );

        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            openAPI.setServers(List.of(new Server().url(publicBaseUrl)));
        }

        return openAPI;
    }
}
