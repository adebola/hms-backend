package io.factorialsystems.communications.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtParser jwtParser;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null && validateToken(token)) {
                Claims claims = jwtParser.parseSignedClaims(token).getPayload();

                String userId = claims.getSubject();
                UUID tenantId = UUID.fromString(claims.get("tenant_id", String.class));

                // Extract permissions/roles from JWT
                @SuppressWarnings("unchecked")
                List<String> permissions = claims.get("permissions", List.class);

                List<SimpleGrantedAuthority> authorities = permissions != null
                        ? permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
                        : List.of();

                // Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Store tenant in ThreadLocal
                TenantContext.setTenantId(tenantId);

                log.debug("Authenticated user: {} for tenant: {}", userId, tenantId);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            TenantContext.clear();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear tenant context after request
            TenantContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
