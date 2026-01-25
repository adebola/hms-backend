package io.factorialsystems.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthServerApplicationTests {
    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}
