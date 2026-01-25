package io.factorialsystems.communications.repository;

import io.factorialsystems.communications.model.entity.DeliveryLog;
import io.factorialsystems.communications.model.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {

    List<DeliveryLog> findByMessageIdOrderByOccurredAtDesc(UUID messageId);

    Page<DeliveryLog> findByTenantId(UUID tenantId, Pageable pageable);

    Page<DeliveryLog> findByTenantIdAndMessageType(UUID tenantId, MessageType messageType, Pageable pageable);

    Page<DeliveryLog> findByTenantIdAndEventType(UUID tenantId, String eventType, Pageable pageable);
}
