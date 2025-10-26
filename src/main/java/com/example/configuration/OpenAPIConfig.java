package com.example.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User API",
                description = "API для взаимодействия с пользователями",
                version = "1.0.0",
                contact = @Contact(
                        name = "documentation creator's name",
                        email = "developer@gmail.com",
                        url = "https://example.com"
                )
        )
)
public class OpenAPIConfig {
}
