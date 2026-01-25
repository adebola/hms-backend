package io.factorialsystems.communications.model.entity;

import io.factorialsystems.communications.model.enums.MessageStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_messages", indexes = {
    @Index(name = "idx_email_messages_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_email_messages_status", columnList = "status"),
    @Index(name = "idx_email_messages_created_at", columnList = "created_at"),
    @Index(name = "idx_email_messages_provider_id", columnList = "provider_id"),
    @Index(name = "idx_email_messages_tenant_status", columnList = "tenant_id, status")
})
public class EmailMessage extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Column(name = "to_name")
    private String toName;

    @Column(name = "from_email", nullable = false)
    private String fromEmail;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status;

    @Column(name = "provider_id")
    private String providerId;

    @Type(JsonBinaryType.class)
    @Column(name = "attachments", columnDefinition = "jsonb")
    private List<Map<String, String>> attachments;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
}
