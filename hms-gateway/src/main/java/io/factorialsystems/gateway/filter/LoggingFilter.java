package io.factorialsystems.gateway.filter;

import io.factorialsystems.gateway.config.HmsGatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for request/response logging.
 *
 * Logs key information about each request:
 * - Request ID (from X-Request-ID header)
 * - HTTP method and path
 * - Response status code
 * - Request latency
 *
 * Excludes actuator endpoints to reduce log noise.
 *
 * Priority: High (runs early, but after RequestIdFilter)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTR = "startTime";

    private final HmsGatewayProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.getLogging().isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip logging for actuator endpoints
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        String method = request.getMethod().name();

        // Record start time
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        log.info(">>> Request: {} {} | RequestID: {}", method, path, requestId);

        if (gatewayProperties.getLogging().isIncludeHeaders()) {
            log.debug("Headers: {}", request.getHeaders());
        }

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logResponse(exchange, requestId, method, path))
                .doOnError(error -> logError(exchange, requestId, method, path, error));
    }

    private void logResponse(ServerWebExchange exchange, String requestId, String method, String path) {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        long startTime = exchange.getAttribute(START_TIME_ATTR);
        long latency = System.currentTimeMillis() - startTime;

        log.info("<<< Response: {} {} | Status: {} | Latency: {}ms | RequestID: {}",
                method, path, statusCode != null ? statusCode.value() : "UNKNOWN", latency, requestId);
    }

    private void logError(ServerWebExchange exchange, String requestId, String method, String path, Throwable error) {
        long startTime = exchange.getAttribute(START_TIME_ATTR);
        long latency = System.currentTimeMillis() - startTime;

        log.error("<<< Error: {} {} | Latency: {}ms | RequestID: {} | Error: {}",
                method, path, latency, requestId, error.getMessage(), error);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Run after RequestIdFilter
    }
}
