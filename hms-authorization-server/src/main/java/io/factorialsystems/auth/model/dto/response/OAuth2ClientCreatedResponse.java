package io.factorialsystems.auth.model.dto.response;

import lombok.*;

/**
 * Response after creating a new OAuth2 client
 * Includes the plain-text client secret (only shown once)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2ClientCreatedResponse {
    private String clientId;
    private String clientSecret; // Plain-text - only shown once!
    private String clientName;
    private String tenantId;
    private String message;

    /**
     * Security warning message
     */
    public static String getSecurityWarning() {
        return "IMPORTANT: Store this client secret securely. " +
               "It will not be shown again and cannot be retrieved. " +
               "If lost, you will need to generate a new secret.";
    }
}
