package io.factorialsystems.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * Cached wrapper for RegisteredClientRepository
 * Caches OAuth2 client lookups in Redis to improve performance
 *
 * Cache Strategy:
 * - Cache on read (findById, findByClientId)
 * - Invalidate all on write (save)
 * - TTL: 30 minutes (configured in application.yml)
 *
 * Performance Impact:
 * - Without cache: ~50-100ms per auth request (database lookup)
 * - With cache: ~1-2ms per auth request (Redis lookup)
 */
@RequiredArgsConstructor
@Slf4j
public class CachedRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientRepository delegate;

    private static final String CACHE_NAME = "oauth2_clients";

    /**
     * Find client by ID with caching
     * Cache key: client ID
     */
    @Override
    @Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
    public RegisteredClient findById(String id) {
        log.debug("Cache miss - fetching client by id from database: {}", id);
        RegisteredClient client = delegate.findById(id);

        if (client != null) {
            log.debug("Client found in database: {} ({})", client.getClientId(), client.getClientName());
        } else {
            log.warn("Client not found by id: {}", id);
        }

        return client;
    }

    /**
     * Find client by client ID with caching
     * Cache key: "client:" + clientId for namespace separation
     */
    @Override
    @Cacheable(value = CACHE_NAME, key = "'client:' + #clientId", unless = "#result == null")
    public RegisteredClient findByClientId(String clientId) {
        log.debug("Cache miss - fetching client by clientId from database: {}", clientId);
        RegisteredClient client = delegate.findByClientId(clientId);

        if (client != null) {
            log.debug("Client found in database: {} ({})", client.getClientId(), client.getClientName());
        } else {
            log.warn("Client not found by clientId: {}", clientId);
        }

        return client;
    }

    /**
     * Save client and invalidate the entire cache
     *
     * Note: We invalidate all entries because:
     * 1. A client can be updated (same ID, different data)
     * 2. We cache by both id and clientId
     * 3. Evicting specific entries is error-prone
     * 4. Client updates are rare, so full invalidation is acceptable
     */
    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void save(RegisteredClient registeredClient) {
        log.info("Saving client and invalidating entire cache: {} ({})",
                registeredClient.getClientId(), registeredClient.getClientName());

        delegate.save(registeredClient);

        log.debug("Client saved successfully and cache invalidated");
    }
}
