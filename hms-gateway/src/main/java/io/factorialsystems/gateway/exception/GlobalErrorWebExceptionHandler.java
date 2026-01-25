package io.factorialsystems.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for the HMS Gateway.
 *
 * Provides consistent error responses across all gateway errors:
 * - Service unavailable (503) - when downstream service is down
 * - Gateway timeout (504) - when downstream service times out
 * - Not found (404) - when route doesn't exist
 * - Internal server error (500) - for unexpected errors
 *
 * All errors return the HMS ApiResponse format for consistency.
 */
@Slf4j
@Component
@Order(-2) // Higher precedence than DefaultErrorWebExceptionHandler
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext,
            ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Throwable error = getError(request);

        String requestId = request.headers().firstHeader("X-Request-ID");
        String path = (String) errorAttributes.get("path");

        HttpStatus status = determineHttpStatus(error);
        Map<String, Object> errorResponse = buildErrorResponse(error, status, requestId, path);

        log.error("Gateway error: {} {} - Status: {} - Error: {} - RequestID: {}",
                request.method(), path, status.value(), error.getMessage(), requestId);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (error instanceof TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (error instanceof org.springframework.web.server.ResponseStatusException statusException) {
            return HttpStatus.valueOf(statusException.getStatusCode().value());
        } else if (error.getMessage() != null && error.getMessage().contains("404")) {
            return HttpStatus.NOT_FOUND;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Map<String, Object> buildErrorResponse(Throwable error, HttpStatus status, String requestId, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", getErrorType(status));
        response.put("message", getErrorMessage(error, status));
        response.put("status", status.value());
        response.put("path", path);
        response.put("requestId", requestId);
        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }

    private String getErrorType(HttpStatus status) {
        return switch (status) {
            case SERVICE_UNAVAILABLE -> "Service Unavailable";
            case GATEWAY_TIMEOUT -> "Gateway Timeout";
            case NOT_FOUND -> "Not Found";
            case BAD_GATEWAY -> "Bad Gateway";
            default -> "Gateway Error";
        };
    }

    private String getErrorMessage(Throwable error, HttpStatus status) {
        return switch (status) {
            case SERVICE_UNAVAILABLE ->
                    "The requested service is currently unavailable. Please try again later.";
            case GATEWAY_TIMEOUT ->
                    "The service took too long to respond. Please try again.";
            case NOT_FOUND ->
                    "The requested endpoint does not exist.";
            case BAD_GATEWAY ->
                    "Received invalid response from the service.";
            default ->
                    "An unexpected error occurred while processing your request.";
        };
    }
}
