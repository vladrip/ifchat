package com.vladrip.ifchat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private Info apiInfo() {
        return new Info().title("IFChat REST API")
                .description("API for \"IFChat\" chat application")
                .version("0.1")
                .contact(new Contact().name("Vladyslav Ripskyi").email("vladrip12@gmail.com"));
    }

    @Bean
    public OpenAPI openApiConfig() {
        return new OpenAPI().info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("firebaseBearer"))
                .components(new Components()
                        .addSecuritySchemes("firebaseBearer", new SecurityScheme()
                                .name("firebaseBearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
