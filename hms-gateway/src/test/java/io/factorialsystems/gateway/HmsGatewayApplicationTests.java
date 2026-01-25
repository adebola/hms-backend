package io.factorialsystems.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic application context test for HMS Gateway.
 *
 * Verifies that the Spring context loads successfully with all configurations.
 */
@SpringBootTest
@ActiveProfiles("test")
class HmsGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Test passes if context loads without errors
    }
}
