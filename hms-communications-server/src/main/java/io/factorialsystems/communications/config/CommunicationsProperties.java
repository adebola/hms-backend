package io.factorialsystems.communications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "hms.communications")
public class CommunicationsProperties {

    private Brevo brevo = new Brevo();
    private Sms sms = new Sms();
    private RateLimit rateLimit = new RateLimit();
    private Message message = new Message();
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Brevo {
        private String apiKey;
        private String defaultFromEmail;
        private String defaultFromName;
    }

    @Getter
    @Setter
    public static class Sms {
        private String provider = "STUB";
        private String accountSid;
        private String authToken;
        private String defaultFromPhone;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Integer defaultDailyEmailLimit = 1000;
        private Integer defaultDailySmsLimit = 100;
    }

    @Getter
    @Setter
    public static class Message {
        private Integer maxAttachmentSizeMb = 25;
        private Integer maxAttachmentsPerEmail = 10;
        private Integer maxRetryAttempts = 3;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private String issuer = "hms-authorization-server";
    }
}
