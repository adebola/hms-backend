package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.AuthAuditLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
    Page<AuthAuditLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
    Page<AuthAuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
