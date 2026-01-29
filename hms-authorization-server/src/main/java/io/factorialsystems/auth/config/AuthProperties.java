package io.factorialsystems.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hms.auth")
public class AuthProperties {

    private JwtProperties jwt = new JwtProperties();
    private PasswordProperties password = new PasswordProperties();
    private LockoutProperties lockout = new LockoutProperties();
    private SessionProperties session = new SessionProperties();
    private PlatformProperties platform = new PlatformProperties();

    @Getter
    @Setter
    public static class JwtProperties {
        private int accessTokenValidityMinutes = 15;
        private int refreshTokenValidityDays = 7;
        private String keyStorePath = "classpath:keys/authserver.jks";
        private String keyStorePassword = "password";
        private String keyAlias = "authserver";
    }

    @Getter
    @Setter
    public static class PasswordProperties {
        private int minLength = 8;
        private int maxLength = 128;
        private boolean requireUppercase = true;
        private boolean requireLowercase = true;
        private boolean requireDigit = true;
        private boolean requireSpecialChar = true;
        private int historyCount = 5;
        private int maxAgeDays = 90;
    }

    @Getter
    @Setter
    public static class LockoutProperties {
        private int maxFailedAttempts = 5;
        private int lockoutDurationMinutes = 30;
        private int resetFailedAttemptsAfterMinutes = 15;
    }

    @Getter
    @Setter
    public static class SessionProperties {
        private int maxConcurrentSessions = 3;
        private int timeoutMinutes = 30;
    }

    @Getter
    @Setter
    public static class PlatformProperties {
        private String adminEmail = "admin@hms-platform.com";
        private String adminPassword = "ChangeMe123!";
        private String location = "http://localhost:9000/auth";
    }
}
