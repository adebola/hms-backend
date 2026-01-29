package io.factorialsystems.auth.service;

import io.factorialsystems.auth.config.AuthProperties;
import io.factorialsystems.auth.exception.DuplicateResourceException;
import io.factorialsystems.auth.exception.ResourceNotFoundException;
import io.factorialsystems.auth.model.dto.request.CreateOAuth2ClientRequest;
import io.factorialsystems.auth.model.dto.response.OAuth2ClientCreatedResponse;
import io.factorialsystems.auth.model.dto.response.OAuth2ClientResponse;
import io.factorialsystems.auth.model.entity.Tenant;
import io.factorialsystems.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing OAuth2 clients for multi-tenant SaaS
 *
 * Key Features:
 * - Auto-create OAuth2 client when tenant registers
 * - Each tenant gets their own client with tenant-specific redirect URIs
 * - Secure client secret generation and rotation
 * - Client lifecycle management (active, suspended, revoked)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2ClientService {

    private final RegisteredClientRepository clientRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Automatically create OAuth2 client for a tenant
     * Called when tenant is registered
     *
     * @param tenantId Tenant ID
     * @return Plain-text client secret (only shown once)
     */
    @Transactional
    public OAuth2ClientCreatedResponse createClientForTenant(UUID tenantId) {
        return createClientForTenant(tenantId, null);
    }

    /**
     * Create OAuth2 client for a tenant with custom username
     *
     * @param tenantId Tenant ID
     * @param createdByUsername Username of admin creating the client
     * @return OAuth2ClientCreatedResponse with client credentials
     */
    @Transactional
    public OAuth2ClientCreatedResponse createClientForTenant(UUID tenantId, String createdByUsername) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId.toString()));

        // Check if client already exists for this tenant
        String clientId = generateClientId(tenant);
        RegisteredClient existing = clientRepository.findByClientId(clientId);
        if (existing != null) {
            throw new DuplicateResourceException("OAuth2Client", "tenantId", tenant.getCode());
        }

        String plainTextSecret = generateClientSecret();

        log.info("Creating OAuth2 client for tenant: {} ({})", tenant.getCode(), tenant.getName());

        RegisteredClient client = buildClient(tenant, clientId, plainTextSecret);

        // Save the client
        clientRepository.save(client);

        // Update audit columns (tenant_id, created_by)
        updateClientAuditInfo(client.getId(), tenantId, createdByUsername);

        log.info("OAuth2 client created successfully: {} for tenant: {}",
                clientId, tenant.getCode());

        return OAuth2ClientCreatedResponse.builder()
                .clientId(clientId)
                .clientSecret(plainTextSecret)
                .clientName(client.getClientName())
                .tenantId(tenantId.toString())
                .message(OAuth2ClientCreatedResponse.getSecurityWarning())
                .build();
    }

    /**
     * Create custom OAuth2 client from admin request
     */
    @Transactional
    public OAuth2ClientCreatedResponse createClient(CreateOAuth2ClientRequest request, String createdBy) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", request.getTenantId().toString()));

        String clientId = request.getClientId() != null ?
                request.getClientId() : generateClientId(tenant);
        String plainTextSecret = request.getClientSecret() != null ?
                request.getClientSecret() : generateClientSecret();

        // Check for duplicates
        if (clientRepository.findByClientId(clientId) != null) {
            throw new DuplicateResourceException("OAuth2Client", "clientId", clientId);
        }

        RegisteredClient client = buildCustomClient(tenant, request, clientId, plainTextSecret);

        clientRepository.save(client);
        updateClientAuditInfo(client.getId(), request.getTenantId(), createdBy);

        log.info("Custom OAuth2 client created: {} for tenant: {} by: {}",
                clientId, tenant.getCode(), createdBy);

        return OAuth2ClientCreatedResponse.builder()
                .clientId(clientId)
                .clientSecret(plainTextSecret)
                .clientName(client.getClientName())
                .tenantId(request.getTenantId().toString())
                .message(OAuth2ClientCreatedResponse.getSecurityWarning())
                .build();
    }

    /**
     * Get OAuth2 client details by client ID
     */
    public OAuth2ClientResponse getClientByClientId(String clientId) {
        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("OAuth2Client", clientId);
        }

        return mapToResponse(client);
    }

    /**
     * Get all OAuth2 clients for a tenant
     */
    public List<OAuth2ClientResponse> getClientsByTenantId(UUID tenantId) {
        String sql = "SELECT * FROM oauth2_registered_client WHERE tenant_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToResponse, tenantId);
    }

    /**
     * Rotate client secret (for security)
     */
    @Transactional
    public String rotateClientSecret(String clientId) {
        RegisteredClient existing = clientRepository.findByClientId(clientId);
        if (existing == null) {
            throw new ResourceNotFoundException("OAuth2Client", clientId);
        }

        String newPlainTextSecret = generateClientSecret();

        RegisteredClient updated = RegisteredClient.from(existing)
                .clientSecret(passwordEncoder.encode(newPlainTextSecret))
                .build();

        clientRepository.save(updated);

        log.info("Client secret rotated for: {}", clientId);

        return newPlainTextSecret;
    }

    /**
     * Suspend OAuth2 client (prevent authentication)
     */
    @Transactional
    public void suspendClient(String clientId) {
        updateClientStatus(clientId, "SUSPENDED");
        log.info("OAuth2 client suspended: {}", clientId);
    }

    /**
     * Activate OAuth2 client
     */
    @Transactional
    public void activateClient(String clientId) {
        updateClientStatus(clientId, "ACTIVE");
        log.info("OAuth2 client activated: {}", clientId);
    }

    /**
     * Revoke OAuth2 client (permanent)
     */
    @Transactional
    public void revokeClient(String clientId) {
        updateClientStatus(clientId, "REVOKED");
        log.info("OAuth2 client revoked: {}", clientId);
    }

    /**
     * Delete OAuth2 client
     */
    @Transactional
    public void deleteClient(String clientId) {
        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("OAuth2Client", clientId);
        }

        String sql = "DELETE FROM oauth2_registered_client WHERE id = ?";
        jdbcTemplate.update(sql, client.getId());

        log.info("OAuth2 client deleted: {}", clientId);
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Build standard OAuth2 client for tenant
     */
    private RegisteredClient buildClient(Tenant tenant, String clientId, String plainTextSecret) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(plainTextSecret))
                .clientName(tenant.getName() + " - Web Client")

                // Authentication methods
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

                // Grant types
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)

                // Redirect URIs - tenant-specific
                .redirectUri(buildTenantRedirectUri(tenant, "/callback"))
                .redirectUri(buildTenantRedirectUri(tenant, "/login/oauth2/code/hms"))
                .redirectUri("http://localhost:4200/callback") // Development
                .redirectUri("http://localhost:3000/callback") // Development (React)

                // Post-logout redirect URIs
                .postLogoutRedirectUri(buildTenantRedirectUri(tenant, "/"))
                .postLogoutRedirectUri("http://localhost:4200/")
                .postLogoutRedirectUri("http://localhost:3000/")

                // Scopes
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("patient:read")
                .scope("patient:write")
                .scope("prescription:read")
                .scope("prescription:write")
                .scope("billing:read")
                .scope("billing:write")

                // Client settings
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false) // Auto-approve for own app
                        .requireProofKey(true) // PKCE required for security
                        .build())

                // Token settings from configuration
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(
                                authProperties.getJwt().getAccessTokenValidityMinutes()))
                        .refreshTokenTimeToLive(Duration.ofDays(
                                authProperties.getJwt().getRefreshTokenValidityDays()))
                        .reuseRefreshTokens(false) // Don't reuse refresh tokens (better security)
                        .build())

                .build();
    }

    /**
     * Build custom OAuth2 client from request
     */
    private RegisteredClient buildCustomClient(Tenant tenant, CreateOAuth2ClientRequest request,
                                                String clientId, String plainTextSecret) {
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(plainTextSecret))
                .clientName(request.getClientName())

                // Standard auth methods and grant types
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);

        // Redirect URIs
        if (request.getRedirectUris() != null && !request.getRedirectUris().isEmpty()) {
            request.getRedirectUris().forEach(builder::redirectUri);
        } else {
            builder.redirectUri(buildTenantRedirectUri(tenant, "/callback"));
        }

        // Post-logout URIs
        if (request.getPostLogoutRedirectUris() != null && !request.getPostLogoutRedirectUris().isEmpty()) {
            request.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
        }

        // Scopes
        if (request.getScopes() != null && !request.getScopes().isEmpty()) {
            request.getScopes().forEach(builder::scope);
        } else {
            builder.scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope(OidcScopes.EMAIL);
        }

        // Client settings
        builder.clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(request.getRequireAuthorizationConsent() != null ?
                        request.getRequireAuthorizationConsent() : false)
                .requireProofKey(request.getRequireProofKey() != null ?
                        request.getRequireProofKey() : true)
                .build());

        // Token settings
        int accessTokenTtl = request.getAccessTokenTtlMinutes() != null ?
                request.getAccessTokenTtlMinutes() :
                authProperties.getJwt().getAccessTokenValidityMinutes();

        int refreshTokenTtl = request.getRefreshTokenTtlDays() != null ?
                request.getRefreshTokenTtlDays() :
                authProperties.getJwt().getRefreshTokenValidityDays();

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTtl))
                .refreshTokenTimeToLive(Duration.ofDays(refreshTokenTtl))
                .reuseRefreshTokens(false)
                .build());

        return builder.build();
    }

    /**
     * Generate client ID based on tenant code
     * Example: "hospital-a-web-client"
     */
    private String generateClientId(Tenant tenant) {
        return tenant.getCode().toLowerCase().replace("_", "-") + "-web-client";
    }

    /**
     * Generate secure random client secret
     * 64 characters hex string
     */
    private String generateClientSecret() {
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Build tenant-specific redirect URI
     * Examples:
     * - https://hospital-a.hms.com/callback
     * - https://clinic-b.hms.com/callback
     */
    private String buildTenantRedirectUri(Tenant tenant, String path) {
        String subdomain = tenant.getCode().toLowerCase().replace("_", "-");
        // TODO: Get base domain from configuration (environment variable)
        String baseDomain = "hms.com"; // Should come from config
        return String.format("https://%s.%s%s", subdomain, baseDomain, path);
    }

    /**
     * Update client audit information (tenant_id, created_by, etc.)
     */
    private void updateClientAuditInfo(String clientId, UUID tenantId, String createdBy) {
        String sql = "UPDATE oauth2_registered_client " +
                "SET tenant_id = ?, created_by = ?, status = 'ACTIVE' " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, tenantId, createdBy, clientId);
    }

    /**
     * Update client status
     */
    private void updateClientStatus(String clientId, String status) {
        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("OAuth2Client", clientId);
        }

        String sql = "UPDATE oauth2_registered_client SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, client.getId());
    }

    /**
     * Map RegisteredClient to response DTO
     */
    private OAuth2ClientResponse mapToResponse(RegisteredClient client) {
        // Get tenant info from database
        String sql = "SELECT t.tenant_code, t.facility_name, c.tenant_id, c.status, " +
                "c.created_by, c.created_at, c.updated_at " +
                "FROM oauth2_registered_client c " +
                "LEFT JOIN tenants t ON c.tenant_id = t.id " +
                "WHERE c.id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            OAuth2ClientResponse response = new OAuth2ClientResponse();
            response.setId(client.getId());
            response.setClientId(client.getClientId());
            response.setClientName(client.getClientName());
            response.setClientIdIssuedAt(client.getClientIdIssuedAt());
            response.setClientSecretExpiresAt(client.getClientSecretExpiresAt());

            response.setClientAuthenticationMethods(client.getClientAuthenticationMethods().stream()
                    .map(ClientAuthenticationMethod::getValue)
                    .collect(Collectors.toSet()));

            response.setAuthorizationGrantTypes(client.getAuthorizationGrantTypes().stream()
                    .map(AuthorizationGrantType::getValue)
                    .collect(Collectors.toSet()));

            response.setRedirectUris(client.getRedirectUris());
            response.setPostLogoutRedirectUris(client.getPostLogoutRedirectUris());
            response.setScopes(client.getScopes());

            // From database
            response.setTenantId(rs.getString("tenant_id"));
            response.setTenantCode(rs.getString("tenant_code"));
            response.setFacilityName(rs.getString("facility_name"));
            response.setStatus(rs.getString("status"));
            response.setCreatedBy(rs.getString("created_by"));

            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                response.setCreatedAt(createdAt.toLocalDateTime());
            }

            java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                response.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            // Token settings
            response.setAccessTokenTtlMinutes(client.getTokenSettings().getAccessTokenTimeToLive().toMinutes());
            response.setRefreshTokenTtlDays(client.getTokenSettings().getRefreshTokenTimeToLive().toDays());
            response.setRequireProofKey(client.getClientSettings().isRequireProofKey());
            response.setRequireAuthorizationConsent(client.getClientSettings().isRequireAuthorizationConsent());

            return response;
        }, client.getId());
    }

    /**
     * Map database row to OAuth2ClientResponse
     */
    private OAuth2ClientResponse mapRowToResponse(ResultSet rs, int rowNum) throws SQLException {
        OAuth2ClientResponse response = new OAuth2ClientResponse();
        response.setId(rs.getString("id"));
        response.setClientId(rs.getString("client_id"));
        response.setClientName(rs.getString("client_name"));
        response.setTenantId(rs.getString("tenant_id"));
        response.setStatus(rs.getString("status"));
        response.setCreatedBy(rs.getString("created_by"));

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            response.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            response.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return response;
    }
}
