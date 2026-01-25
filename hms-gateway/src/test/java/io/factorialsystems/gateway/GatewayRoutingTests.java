package io.factorialsystems.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HMS Gateway routing and filters.
 *
 * Tests:
 * - Route configuration and resolution
 * - Request ID header generation
 * - CORS headers
 * - Health check endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void shouldLoadRoutes() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<Route> routeList = routes.collectList().block();

        assertThat(routeList)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(5); // At least auth, communications, patient, and health routes
    }

    @Test
    void shouldHaveAuthenticationRoute() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<Route> routeList = routes.collectList().block();

        boolean hasAuthRoute = routeList.stream()
                .anyMatch(route -> route.getId().equals("auth-authentication"));

        assertThat(hasAuthRoute).isTrue();
    }

    @Test
    void shouldHaveCommunicationsRoute() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<Route> routeList = routes.collectList().block();

        boolean hasCommRoute = routeList.stream()
                .anyMatch(route -> route.getId().equals("communications-email"));

        assertThat(hasCommRoute).isTrue();
    }

    @Test
    void shouldHavePatientRoute() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<Route> routeList = routes.collectList().block();

        boolean hasPatientRoute = routeList.stream()
                .anyMatch(route -> route.getId().equals("patient-management"));

        assertThat(hasPatientRoute).isTrue();
    }

    @Test
    void shouldAddRequestIdHeader() {
        // Test that gateway adds X-Request-ID when not present
        // Note: This will fail with 503 (service unavailable) but that's expected in tests
        // We're just verifying the header is added
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectHeader().exists("X-Request-ID");
    }

    @Test
    void shouldPreserveExistingRequestId() {
        String customRequestId = "test-request-123";

        webTestClient.get()
                .uri("/actuator/health")
                .header("X-Request-ID", customRequestId)
                .exchange()
                .expectHeader().valueEquals("X-Request-ID", customRequestId);
    }

    @Test
    void shouldReturnCorsHeaders() {
        webTestClient.options()
                .uri("/api/v1/auth/login")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().exists("Access-Control-Allow-Methods");
    }

    @Test
    void shouldExposeHealthEndpoint() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").exists();
    }
}
