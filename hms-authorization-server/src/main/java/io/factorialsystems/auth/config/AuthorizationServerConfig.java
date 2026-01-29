package io.factorialsystems.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {
    private final AuthProperties authProperties;
    private final ResourceLoader resourceLoader;

    private static final String systemClientId = "hms-system-client";

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository jdbcRepository = new JdbcRegisteredClientRepository(jdbcTemplate);

        // Register system/platform client (for admin/platform operations)
        registerSystemClient(jdbcRepository);

        // Wrap with caching layer for performance
        return new CachedRegisteredClientRepository(jdbcRepository);
    }

    /**
     * Register system/platform OAuth2 client
     * This client is used for:
     * - Platform-level administration
     * - Cross-tenant operations by super admins
     * - Internal service-to-service communication
     *
     * Note: Individual tenant clients are created automatically via OAuth2ClientService
     */
    private void registerSystemClient(JdbcRegisteredClientRepository repository) {
        if (repository.findByClientId(systemClientId) == null) {
            log.warn("Unable to Load System Client {} from database, creating default client", systemClientId);
            RegisteredClient systemClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(systemClientId)
                    .clientSecret("{noop}hms-system-secret") // TODO: Use env variable in production
                    .clientName("HMS System Client")

                    // Authentication methods
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

                    // Grant types
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)

                    // System-level redirect URIs
                    .redirectUri("http://localhost:3000/callback")
                    .redirectUri("http://localhost:4200/callback")
                    .redirectUri("https://admin.hms-platform.com/callback")

                    // Scopes
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("system:admin")
                    .scope("tenant:manage")

                    // Client settings
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(true)
                            .build())

                    // Token settings
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(authProperties.getJwt().getAccessTokenValidityMinutes()))
                            .refreshTokenTimeToLive(Duration.ofDays(authProperties.getJwt().getRefreshTokenValidityDays()))
                            .reuseRefreshTokens(false)
                            .build())

                    .build();

            repository.save(systemClient);
        }
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository rcr) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, rcr);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository rcr) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, rcr);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = loadRsaKeyFromKeyStore();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        log.info("JWT signing keys loaded from keystore: {}", authProperties.getJwt().getKeyStorePath());
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    /**
     * Load RSA key pair from Java KeyStore (JKS)
     * Uses configuration from application.yml:
     * - hms.auth.jwt.key-store-path
     * - hms.auth.jwt.key-store-password
     * - hms.auth.jwt.key-alias
     */
    private KeyPair loadRsaKeyFromKeyStore() {
        try {
            // Get keystore configuration
            String keyStorePath = authProperties.getJwt().getKeyStorePath();
            String keyStorePassword = authProperties.getJwt().getKeyStorePassword();
            String keyAlias = authProperties.getJwt().getKeyAlias();

            // Load keystore file
            Resource resource = resourceLoader.getResource(keyStorePath);
            KeyStore keyStore = KeyStore.getInstance("JKS");

            try (InputStream inputStream = resource.getInputStream()) {
                keyStore.load(inputStream, keyStorePassword.toCharArray());
            }

            // Load key pair from keystore
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, keyPassword);

            if (privateKeyEntry == null) {
                throw new IllegalStateException("Key alias '" + keyAlias + "' not found in keystore");
            }

            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA key pair from keystore: " + authProperties.getJwt().getKeyStorePath(), e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(authProperties.getPlatform().getLocation())
                .build();
    }
}
