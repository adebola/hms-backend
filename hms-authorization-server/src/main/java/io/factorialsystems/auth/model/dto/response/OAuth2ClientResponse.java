package io.factorialsystems.auth.model.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for OAuth2 Client information
 * Used to return client details to admins (without secrets)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2ClientResponse {
    private String id;
    private String clientId;
    private String clientName;
    private String tenantId;
    private String tenantCode;
    private String facilityName;

    private Instant clientIdIssuedAt;
    private Instant clientSecretExpiresAt;

    private Set<String> clientAuthenticationMethods;
    private Set<String> authorizationGrantTypes;
    private Set<String> redirectUris;
    private Set<String> postLogoutRedirectUris;
    private Set<String> scopes;

    private String status; // ACTIVE, SUSPENDED, REVOKED

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Token settings (for display)
    private Long accessTokenTtlMinutes;
    private Long refreshTokenTtlDays;
    private Boolean requireProofKey;
    private Boolean requireAuthorizationConsent;
}
