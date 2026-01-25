package io.factorialsystems.auth.service;

import io.factorialsystems.auth.model.entity.AuthAuditLog;
import io.factorialsystems.auth.model.enums.AuthEventType;
import io.factorialsystems.auth.repository.AuthAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuthAuditLogRepository auditLogRepository;

    @Async
    public void logAuthEvent(UUID tenantId, UUID userId, String username, AuthEventType eventType, String ipAddress, String userAgent, boolean success, String failureReason) {
        try {
            AuthAuditLog auditLog = AuthAuditLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .username(username)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .failureReason(failureReason)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} for user: {} - success: {}", eventType, username, success);
        } catch (Exception e) { log.error("Failed to save audit log: {}", e.getMessage(), e); }
    }
}
