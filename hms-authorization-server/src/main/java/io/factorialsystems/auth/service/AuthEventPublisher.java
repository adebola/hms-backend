package io.factorialsystems.auth.service;

import io.factorialsystems.auth.config.RabbitMQConfig;
import io.factorialsystems.auth.model.entity.Tenant;
import io.factorialsystems.auth.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Async public void publishTenantCreated(Tenant tenant) { publishEvent("tenant.created", buildTenantPayload(tenant)); }
    @Async public void publishTenantActivated(Tenant tenant) { publishEvent("tenant.activated", buildTenantPayload(tenant)); }
    @Async public void publishTenantSuspended(Tenant tenant) { publishEvent("tenant.suspended", buildTenantPayload(tenant)); }
    @Async public void publishUserCreated(User user) { publishEvent("user.created", buildUserPayload(user)); }

    private void publishEvent(String routingKey, Map<String, String> payload) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("messageId", UUID.randomUUID().toString());
            message.put("messageType", routingKey);
            message.put("timestamp", LocalDateTime.now().toString());
            message.put("payload", payload);
            rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EVENTS_EXCHANGE, routingKey, message);
            log.debug("Published event: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", routingKey, e.getMessage(), e);
        }
    }

    private Map<String, String> buildTenantPayload(Tenant tenant) {
        Map<String, String> payload = new HashMap<>();
        payload.put("tenantId", tenant.getId().toString());
        payload.put("code", tenant.getCode());
        payload.put("name", tenant.getName());
        payload.put("status", tenant.getStatus().name());
        return payload;
    }

    private Map<String, String> buildUserPayload(User user) {
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", user.getId().toString());
        payload.put("tenantId", user.getTenant().getId().toString());
        payload.put("username", user.getUsername());
        payload.put("status", user.getStatus().name());
        return payload;
    }
}
