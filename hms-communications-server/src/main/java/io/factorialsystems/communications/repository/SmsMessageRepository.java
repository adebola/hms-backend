package io.factorialsystems.communications.repository;

import io.factorialsystems.communications.model.entity.SmsMessage;
import io.factorialsystems.communications.model.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, UUID> {

    Page<SmsMessage> findByTenantId(UUID tenantId, Pageable pageable);

    Page<SmsMessage> findByTenantIdAndStatus(UUID tenantId, MessageStatus status, Pageable pageable);

    Optional<SmsMessage> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<SmsMessage> findByProviderIdAndTenantId(String providerId, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, MessageStatus status);
}
