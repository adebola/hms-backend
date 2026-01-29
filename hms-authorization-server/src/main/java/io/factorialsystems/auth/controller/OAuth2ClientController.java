package io.factorialsystems.auth.controller;

import io.factorialsystems.auth.model.dto.request.CreateOAuth2ClientRequest;
import io.factorialsystems.auth.model.dto.response.ApiResponse;
import io.factorialsystems.auth.model.dto.response.OAuth2ClientCreatedResponse;
import io.factorialsystems.auth.model.dto.response.OAuth2ClientResponse;
import io.factorialsystems.auth.service.OAuth2ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OAuth2 Client Management API
 *
 * Provides endpoints for:
 * - Creating OAuth2 clients for tenants
 * - Viewing client details (without secrets)
 * - Rotating client secrets
 * - Suspending/activating/revoking clients
 * - Deleting clients
 *
 * Security:
 * - Only platform admins can create/manage clients
 * - Tenant admins can view their own clients
 * - Client secrets are only shown once during creation
 */
@RestController
@RequestMapping("/api/v1/oauth2/clients")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Client Management", description = "OAuth2 client registration and management")
@SecurityRequirement(name = "bearer-auth")
public class OAuth2ClientController {

    private final OAuth2ClientService oauth2ClientService;

    /**
     * Create a new OAuth2 client for a tenant
     * Only accessible by SUPER_ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAuthority('oauth_client:create')")
    @Operation(
        summary = "Create OAuth2 client",
        description = "Create a new OAuth2 client for a tenant. " +
                     "Client secret will be returned and shown only once."
    )
    public ResponseEntity<ApiResponse<OAuth2ClientCreatedResponse>> createClient(
            @Valid @RequestBody CreateOAuth2ClientRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String currentUser = jwt.getSubject();
        OAuth2ClientCreatedResponse response = oauth2ClientService.createClient(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "OAuth2 client created successfully. " +
                                                    "Store the client secret securely - it will not be shown again."));
    }

    /**
     * Get OAuth2 client details by client ID
     * Accessible by admins
     */
    @GetMapping("/{clientId}")
    @PreAuthorize("hasAuthority('oauth_client:read')")
    @Operation(
        summary = "Get OAuth2 client details",
        description = "Retrieve OAuth2 client details by client ID. Client secret is not returned."
    )
    public ResponseEntity<ApiResponse<OAuth2ClientResponse>> getClient(@PathVariable String clientId) {
        OAuth2ClientResponse response = oauth2ClientService.getClientByClientId(clientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all OAuth2 clients for a tenant
     * Tenant admins can view their own clients
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('oauth_client:read')")
    @Operation(
        summary = "Get tenant's OAuth2 clients",
        description = "Retrieve all OAuth2 clients for a specific tenant"
    )
    public ResponseEntity<ApiResponse<List<OAuth2ClientResponse>>> getClientsByTenant(
            @PathVariable UUID tenantId) {

        List<OAuth2ClientResponse> clients = oauth2ClientService.getClientsByTenantId(tenantId);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    /**
     * Get OAuth2 clients for current user's tenant
     */
    @GetMapping("/my-tenant")
    @Operation(
        summary = "Get my tenant's OAuth2 clients",
        description = "Retrieve OAuth2 clients for current user's tenant"
    )
    public ResponseEntity<ApiResponse<List<OAuth2ClientResponse>>> getMyTenantClients(
            @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaimAsString("tenant_id");
        List<OAuth2ClientResponse> clients = oauth2ClientService.getClientsByTenantId(UUID.fromString(tenantId));
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    /**
     * Rotate client secret
     * Returns new plain-text secret (shown only once)
     */
    @PostMapping("/{clientId}/rotate-secret")
    @PreAuthorize("hasAuthority('oauth_client:update')")
    @Operation(
        summary = "Rotate client secret",
        description = "Generate a new client secret. The new secret is returned and shown only once."
    )
    public ResponseEntity<ApiResponse<Map<String, String>>> rotateSecret(@PathVariable String clientId) {
        String newSecret = oauth2ClientService.rotateClientSecret(clientId);

        Map<String, String> response = Map.of(
                "clientId", clientId,
                "clientSecret", newSecret,
                "warning", "Store this secret securely - it will not be shown again"
        );

        return ResponseEntity.ok(ApiResponse.success(response,
                "Client secret rotated successfully. Store the new secret securely."));
    }

    /**
     * Suspend OAuth2 client (prevents authentication)
     */
    @PostMapping("/{clientId}/suspend")
    @PreAuthorize("hasAuthority('oauth_client:update')")
    @Operation(
        summary = "Suspend OAuth2 client",
        description = "Suspend an OAuth2 client to prevent authentication"
    )
    public ResponseEntity<ApiResponse<Void>> suspendClient(@PathVariable String clientId) {
        oauth2ClientService.suspendClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(null, "Client suspended successfully"));
    }

    /**
     * Activate OAuth2 client
     */
    @PostMapping("/{clientId}/activate")
    @PreAuthorize("hasAuthority('oauth_client:update')")
    @Operation(
        summary = "Activate OAuth2 client",
        description = "Activate a suspended OAuth2 client"
    )
    public ResponseEntity<ApiResponse<Void>> activateClient(@PathVariable String clientId) {
        oauth2ClientService.activateClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(null, "Client activated successfully"));
    }

    /**
     * Revoke OAuth2 client (permanent)
     */
    @PostMapping("/{clientId}/revoke")
    @PreAuthorize("hasAuthority('oauth_client:delete')")
    @Operation(
        summary = "Revoke OAuth2 client",
        description = "Permanently revoke an OAuth2 client"
    )
    public ResponseEntity<ApiResponse<Void>> revokeClient(@PathVariable String clientId) {
        oauth2ClientService.revokeClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(null, "Client revoked successfully"));
    }

    /**
     * Delete OAuth2 client
     */
    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasAuthority('oauth_client:delete')")
    @Operation(
        summary = "Delete OAuth2 client",
        description = "Permanently delete an OAuth2 client"
    )
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable String clientId) {
        oauth2ClientService.deleteClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
    }
}
