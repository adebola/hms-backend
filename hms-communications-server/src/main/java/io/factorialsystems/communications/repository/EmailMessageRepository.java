package io.factorialsystems.communications.repository;

import io.factorialsystems.communications.model.entity.EmailMessage;
import io.factorialsystems.communications.model.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    Page<EmailMessage> findByTenantId(UUID tenantId, Pageable pageable);

    Page<EmailMessage> findByTenantIdAndStatus(UUID tenantId, MessageStatus status, Pageable pageable);

    Optional<EmailMessage> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<EmailMessage> findByProviderIdAndTenantId(String providerId, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, MessageStatus status);
}
