package io.factorialsystems.communications.model.entity;

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
@Table(name = "tenant_settings", indexes = {
    @Index(name = "idx_tenant_settings_tenant_id", columnList = "tenant_id")
})
public class TenantSettings extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    // Email settings
    @Column(name = "default_from_email")
    private String defaultFromEmail;

    @Column(name = "default_from_name")
    private String defaultFromName;

    @Column(name = "email_signature", columnDefinition = "TEXT")
    private String emailSignature;

    // SMS settings
    @Column(name = "default_from_phone", length = 20)
    private String defaultFromPhone;

    // Rate limits
    @Column(name = "daily_email_limit", nullable = false)
    @Builder.Default
    private Integer dailyEmailLimit = 1000;

    @Column(name = "daily_sms_limit", nullable = false)
    @Builder.Default
    private Integer dailySmsLimit = 100;

    // Current counters
    @Column(name = "emails_sent_today", nullable = false)
    @Builder.Default
    private Integer emailsSentToday = 0;

    @Column(name = "sms_sent_today", nullable = false)
    @Builder.Default
    private Integer smsSentToday = 0;

    @Column(name = "limit_reset_date", nullable = false)
    @Builder.Default
    private LocalDateTime limitResetDate = LocalDateTime.now();

    // Tracking settings
    @Column(name = "enable_open_tracking", nullable = false)
    @Builder.Default
    private Boolean enableOpenTracking = true;

    @Column(name = "enable_click_tracking", nullable = false)
    @Builder.Default
    private Boolean enableClickTracking = true;
}
