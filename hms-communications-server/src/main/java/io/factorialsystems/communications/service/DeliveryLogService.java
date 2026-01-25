package io.factorialsystems.communications.service;

import io.factorialsystems.communications.model.entity.DeliveryLog;
import io.factorialsystems.communications.model.enums.MessageType;
import io.factorialsystems.communications.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryLogService {

    private final DeliveryLogRepository deliveryLogRepository;

    public void logEvent(UUID messageId, MessageType messageType, UUID tenantId,
                        String eventType, String providerName,
                        Map<String, Object> eventData, Map<String, Object> providerResponse) {

        DeliveryLog deliveryLog = DeliveryLog.builder()
                .messageId(messageId)
                .messageType(messageType)
                .tenantId(tenantId)
                .eventType(eventType)
                .eventData(eventData)
                .providerName(providerName)
                .providerResponse(providerResponse)
                .occurredAt(LocalDateTime.now())
                .build();

        deliveryLogRepository.save(deliveryLog);

        log.debug("Delivery event logged: {} for message: {}", eventType, messageId);
    }

    public void logEmailSent(UUID messageId, UUID tenantId, String providerId, Map<String, Object> providerResponse) {
        logEvent(messageId, MessageType.EMAIL, tenantId, "sent", "brevo",
                Map.of("providerId", providerId), providerResponse);
    }

    public void logEmailDelivered(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.EMAIL, tenantId, "delivered", "brevo", eventData, null);
    }

    public void logEmailBounced(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.EMAIL, tenantId, "bounced", "brevo", eventData, null);
    }

    public void logEmailOpened(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.EMAIL, tenantId, "opened", "brevo", eventData, null);
    }

    public void logEmailClicked(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.EMAIL, tenantId, "clicked", "brevo", eventData, null);
    }

    public void logSmsSent(UUID messageId, UUID tenantId, String providerId, Map<String, Object> providerResponse) {
        logEvent(messageId, MessageType.SMS, tenantId, "sent", "twilio",
                Map.of("providerId", providerId), providerResponse);
    }

    public void logSmsDelivered(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.SMS, tenantId, "delivered", "twilio", eventData, null);
    }

    public void logSmsFailed(UUID messageId, UUID tenantId, Map<String, Object> eventData) {
        logEvent(messageId, MessageType.SMS, tenantId, "failed", "twilio", eventData, null);
    }
}
