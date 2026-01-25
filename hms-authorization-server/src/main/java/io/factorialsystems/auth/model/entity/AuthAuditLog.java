package io.factorialsystems.auth.model.entity;

import io.factorialsystems.auth.model.enums.AuthEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.*;

@Entity @Table(name = "auth_audit_log") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id") private UUID tenantId;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "username", length = 100) private String username;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 50) private AuthEventType eventType;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "user_agent", length = 500) private String userAgent;
    @Column(name = "success", nullable = false) private boolean success;
    @Column(name = "failure_reason", length = 255) private String failureReason;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "details", columnDefinition = "jsonb") private Map<String, Object> details;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
