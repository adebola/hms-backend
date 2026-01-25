package io.factorialsystems.auth.security;

import io.factorialsystems.auth.config.AuthProperties;
import io.factorialsystems.auth.model.entity.Role;
import io.factorialsystems.auth.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final AuthProperties authProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private SecretKey secretKey;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @PostConstruct
    public void init() {
        String secret = authProperties.getJwt().getKeyStorePassword();
        String paddedSecret = String.format("%-64s", secret).replace(' ', 'X');
        this.secretKey = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", user.getTenant().getId().toString());
        claims.put("tenant_code", user.getTenant().getCode());
        claims.put("user_id", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
        claims.put("permissions", user.getAllPermissions());
        claims.put("token_type", "access");
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofMinutes(authProperties.getJwt().getAccessTokenValidityMinutes()).toMillis());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .id(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", user.getTenant().getId().toString());
        claims.put("user_id", user.getId().toString());
        claims.put("token_type", "refresh");
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofDays(authProperties.getJwt().getRefreshTokenValidityDays()).toMillis());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .id(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            String tokenId = getTokenIdFromToken(token);
            if (tokenId != null && isTokenBlacklisted(tokenId)) return false;
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage()); return false;
        }
    }

    public String getUserIdFromToken(String token) { return getClaims(token).getSubject(); }
    public String getTenantIdFromToken(String token) { return getClaims(token).get("tenant_id", String.class); }
    public String getTokenIdFromToken(String token) { try { return getClaims(token).getId(); } catch (Exception e) { return null; } }

    public void invalidateToken(String token) {
        try {
            String tokenId = getTokenIdFromToken(token);
            if (tokenId != null) {
                Claims claims = getClaims(token);
                long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (ttl > 0) redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + tokenId, "invalidated", Duration.ofMillis(ttl));
            }
        } catch (Exception e) { log.warn("Failed to invalidate token: {}", e.getMessage()); }
    }

    private boolean isTokenBlacklisted(String tokenId) { try { return redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + tokenId); } catch (Exception e) { return false; } }
    private Claims getClaims(String token) { return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload(); }
}
