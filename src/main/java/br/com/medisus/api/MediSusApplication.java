package br.com.medisus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MediSUS API", version = "v1",
        description = "Public healthcare medication inventory and notification API"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class MediSusApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediSusApplication.class, args);
    }
}
