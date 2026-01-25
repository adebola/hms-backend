package io.factorialsystems.communications.model.entity;

import io.factorialsystems.communications.model.enums.MessageType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_logs", indexes = {
    @Index(name = "idx_delivery_logs_message_id", columnList = "message_id"),
    @Index(name = "idx_delivery_logs_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_delivery_logs_event_type", columnList = "event_type"),
    @Index(name = "idx_delivery_logs_occurred_at", columnList = "occurred_at"),
    @Index(name = "idx_delivery_logs_message_type", columnList = "message_type"),
    @Index(name = "idx_delivery_logs_tenant_event", columnList = "tenant_id, event_type")
})
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Type(JsonBinaryType.class)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Type(JsonBinaryType.class)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Map<String, Object> providerResponse;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
