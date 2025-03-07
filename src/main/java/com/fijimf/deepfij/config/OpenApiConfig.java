package com.fijimf.deepfij.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // Add the security scheme with a name (e.g., "BearerAuth")
        Components components = new Components()
                .addSecuritySchemes("BearerAuth", securityScheme);

        // Add a global security requirement to apply the scheme to all endpoints
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth");

        // Build and return the OpenAPI configuration
        return new OpenAPI()
                .info(new Info()
                        .title("Your API Title")
                        .description("API documentation with JWT Authentication")
                        .version("1.0.0"))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}