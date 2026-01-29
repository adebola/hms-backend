package io.factorialsystems.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Request to create a new OAuth2 client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOAuth2ClientRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    // Optional: if not provided, will be auto-generated
    private String clientId;

    // Optional: if not provided, will be auto-generated
    private String clientSecret;

    private Set<String> redirectUris;
    private Set<String> postLogoutRedirectUris;
    private Set<String> scopes;

    // Token lifetime settings (minutes for access, days for refresh)
    private Integer accessTokenTtlMinutes;
    private Integer refreshTokenTtlDays;

    private Boolean requireProofKey; // Default: true
    private Boolean requireAuthorizationConsent; // Default: false
}
