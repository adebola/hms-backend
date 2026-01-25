package io.factorialsystems.communications.service;

import io.factorialsystems.communications.exception.MessageSendException;
import io.factorialsystems.communications.exception.RateLimitExceededException;
import io.factorialsystems.communications.exception.ResourceNotFoundException;
import io.factorialsystems.communications.mapper.EmailMessageMapper;
import io.factorialsystems.communications.model.dto.request.SendEmailRequest;
import io.factorialsystems.communications.model.dto.response.EmailMessageResponse;
import io.factorialsystems.communications.model.entity.EmailMessage;
import io.factorialsystems.communications.model.entity.TenantSettings;
import io.factorialsystems.communications.model.enums.MessageStatus;
import io.factorialsystems.communications.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final EmailMessageRepository emailMessageRepository;
    private final BrevoEmailProvider brevoProvider;
    private final RateLimitService rateLimitService;
    private final TenantSettingsService tenantSettingsService;
    private final DeliveryLogService deliveryLogService;
    private final EmailMessageMapper emailMessageMapper;

    public EmailMessageResponse sendEmail(UUID tenantId, SendEmailRequest request) {
        // Check rate limit
        if (!rateLimitService.checkEmailLimit(tenantId)) {
            throw new RateLimitExceededException("Daily email limit exceeded for tenant");
        }

        // Get tenant settings for defaults
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);

        // Use tenant defaults if not provided in request
        String fromEmail = request.getFromEmail() != null
                ? request.getFromEmail()
                : settings.getDefaultFromEmail();
        String fromName = request.getFromName() != null
                ? request.getFromName()
                : settings.getDefaultFromName();

        // Validate content
        if (request.getHtmlContent() == null && request.getTextContent() == null) {
            throw new MessageSendException("Either htmlContent or textContent must be provided");
        }

        // Convert attachments to proper format
        var attachments = request.getAttachments() != null
                ? request.getAttachments().stream()
                    .map(att -> Map.of(
                            "filename", att.getFilename(),
                            "content", att.getContent(),
                            "contentType", att.getContentType()
                    ))
                    .collect(Collectors.toList())
                : null;

        // Create message record
        EmailMessage message = EmailMessage.builder()
                .tenantId(tenantId)
                .toEmail(request.getToEmail())
                .toName(request.getToName())
                .fromEmail(fromEmail)
                .fromName(fromName)
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .textContent(request.getTextContent())
                .attachments(attachments)
                .status(MessageStatus.PENDING)
                .retryCount(0)
                .build();

        message = emailMessageRepository.save(message);

        try {
            // Send via Brevo
            String providerId = brevoProvider.sendEmail(message);

            // Update status
            message.setProviderId(providerId);
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message = emailMessageRepository.save(message);

            // Log delivery event
            deliveryLogService.logEmailSent(message.getId(), tenantId, providerId,
                    Map.of("messageId", providerId));

            // Increment counter
            rateLimitService.incrementEmailCount(tenantId);

            log.info("Email sent successfully to {} for tenant {}", request.getToEmail(), tenantId);

            return emailMessageMapper.toResponse(message);

        } catch (MessageSendException e) {
            message.setStatus(MessageStatus.FAILED);
            message.setErrorMessage(e.getMessage());
            message.setLastRetryAt(LocalDateTime.now());
            emailMessageRepository.save(message);

            log.error("Failed to send email: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<EmailMessageResponse> listEmails(UUID tenantId, MessageStatus status, Pageable pageable) {
        Page<EmailMessage> messages = status != null
                ? emailMessageRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                : emailMessageRepository.findByTenantId(tenantId, pageable);

        return messages.map(emailMessageMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EmailMessageResponse getEmailById(UUID id, UUID tenantId) {
        EmailMessage message = emailMessageRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Email message not found"));

        return emailMessageMapper.toResponse(message);
    }

    public void retryFailedEmail(UUID messageId, UUID tenantId) {
        EmailMessage message = emailMessageRepository.findByIdAndTenantId(messageId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Email message not found"));

        if (message.getStatus() != MessageStatus.FAILED) {
            throw new MessageSendException("Only failed messages can be retried");
        }

        if (message.getRetryCount() >= 3) {
            throw new MessageSendException("Maximum retry attempts exceeded");
        }

        try {
            String providerId = brevoProvider.sendEmail(message);

            message.setProviderId(providerId);
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setRetryCount(message.getRetryCount() + 1);
            message.setLastRetryAt(LocalDateTime.now());
            message.setErrorMessage(null);

            emailMessageRepository.save(message);

            deliveryLogService.logEmailSent(message.getId(), tenantId, providerId,
                    Map.of("messageId", providerId, "retry", message.getRetryCount()));

            log.info("Email retry successful for message: {}", messageId);

        } catch (MessageSendException e) {
            message.setRetryCount(message.getRetryCount() + 1);
            message.setLastRetryAt(LocalDateTime.now());
            message.setErrorMessage(e.getMessage());
            emailMessageRepository.save(message);

            log.error("Email retry failed for message: {}", messageId);
            throw e;
        }
    }
}
