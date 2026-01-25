package io.factorialsystems.communications;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "HMS Communications Server API",
        version = "1.0.0",
        description = "Email and SMS communications microservice for HMS Platform",
        contact = @Contact(
            name = "Factorial Systems",
            email = "support@factorialsystems.io"
        )
    ),
    servers = {
        @Server(url = "http://localhost:9001/communications", description = "Development Server"),
        @Server(url = "/communications", description = "Current Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class HmsCommunicationsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmsCommunicationsServerApplication.class, args);
    }
}
