package io.factorialsystems.communications.model.entity;

import io.factorialsystems.communications.model.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sms_messages", indexes = {
    @Index(name = "idx_sms_messages_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_sms_messages_status", columnList = "status"),
    @Index(name = "idx_sms_messages_created_at", columnList = "created_at"),
    @Index(name = "idx_sms_messages_provider_id", columnList = "provider_id"),
    @Index(name = "idx_sms_messages_tenant_status", columnList = "tenant_id, status")
})
public class SmsMessage extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "to_phone", nullable = false, length = 20)
    private String toPhone;

    @Column(name = "from_phone", length = 20)
    private String fromPhone;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
}
