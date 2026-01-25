package io.factorialsystems.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for HMS Gateway.
 *
 * Binds to 'gateway' prefix in application.yml.
 * Provides type-safe access to service URLs and gateway settings.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private ServiceUrls services = new ServiceUrls();
    private CorsProperties cors = new CorsProperties();
    private LoggingProperties logging = new LoggingProperties();

    @Getter
    @Setter
    public static class ServiceUrls {
        private String auth;
        private String communications;
        private String patient;
        private String prescription;
        private String billing;
        private String appointment;
        private String lab;
        private String pharmacy;
        private String reporting;
    }

    @Getter
    @Setter
    public static class CorsProperties {
        private String[] allowedOrigins;
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
        private String[] allowedHeaders = {"*"};
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    @Getter
    @Setter
    public static class LoggingProperties {
        private boolean enabled = true;
        private boolean includeHeaders = false;
        private boolean includePayload = false;
        private int maxPayloadLength = 1000;
    }
}
