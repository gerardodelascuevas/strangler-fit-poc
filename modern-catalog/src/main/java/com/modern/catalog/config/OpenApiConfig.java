package com.modern.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Modern Catalog API - Strangler Fig Demo")
                        .version("2.0.0")
                        .description("""
                                Modern REST API for product catalog reporting.
                                
                                This API is part of a Strangler Fig (Strangler Fig) pattern demonstration.
                                New functionality is built here while legacy SOAP services remain untouched.
                                
                                **Strategy:**
                                1. Identify a module to strangulate (reporting)
                                2. Build it independently with modern practices
                                3. Route traffic to the new service
                                4. Retire the legacy equivalent
                                """)
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev-team@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
