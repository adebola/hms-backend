package io.factorialsystems.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HMS API Gateway - Entry point for all HMS platform requests.
 *
 * This gateway provides:
 * - Centralized routing to all HMS microservices
 * - CORS handling for web clients
 * - Request ID generation for distributed tracing
 * - Request/response logging
 * - Health check aggregation
 *
 * Security: JWT validation is delegated to individual services
 */
@SpringBootApplication
public class HmsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmsGatewayApplication.class, args);
    }
}
