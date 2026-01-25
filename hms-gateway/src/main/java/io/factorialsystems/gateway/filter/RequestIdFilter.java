package io.factorialsystems.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that adds a unique X-Request-ID header to every request.
 *
 * This enables distributed tracing across all HMS microservices.
 * If the client already provides an X-Request-ID, it will be preserved.
 * Otherwise, a new UUID will be generated.
 *
 * Priority: Highest (runs first) to ensure request ID is available for all subsequent filters.
 */
@Slf4j
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(REQUEST_ID_HEADER, requestId)
                    .build();

            exchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.debug("Generated new request ID: {}", requestId);
        } else {
            log.debug("Using existing request ID: {}", requestId);
        }

        // Also add to response headers for client visibility
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
