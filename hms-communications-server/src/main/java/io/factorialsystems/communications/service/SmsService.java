package io.factorialsystems.communications.service;

import io.factorialsystems.communications.exception.RateLimitExceededException;
import io.factorialsystems.communications.exception.ResourceNotFoundException;
import io.factorialsystems.communications.mapper.SmsMessageMapper;
import io.factorialsystems.communications.model.dto.request.SendSmsRequest;
import io.factorialsystems.communications.model.dto.response.SmsMessageResponse;
import io.factorialsystems.communications.model.entity.SmsMessage;
import io.factorialsystems.communications.model.entity.TenantSettings;
import io.factorialsystems.communications.model.enums.MessageStatus;
import io.factorialsystems.communications.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SmsService {

    private final SmsMessageRepository smsMessageRepository;
    private final RateLimitService rateLimitService;
    private final TenantSettingsService tenantSettingsService;
    private final SmsMessageMapper smsMessageMapper;

    public SmsMessageResponse sendSms(UUID tenantId, SendSmsRequest request) {
        // Check rate limit
        if (!rateLimitService.checkSmsLimit(tenantId)) {
            throw new RateLimitExceededException("Daily SMS limit exceeded for tenant");
        }

        // Get tenant settings for defaults
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);

        // Use tenant default if not provided in request
        String fromPhone = request.getFromPhone() != null
                ? request.getFromPhone()
                : settings.getDefaultFromPhone();

        // Create message record
        SmsMessage message = SmsMessage.builder()
                .tenantId(tenantId)
                .toPhone(request.getToPhone())
                .fromPhone(fromPhone)
                .message(request.getMessage())
                .status(MessageStatus.PENDING)
                .retryCount(0)
                .build();

        message = smsMessageRepository.save(message);

        log.info("SMS stub: Would send to {} from tenant {} (message: {})",
                request.getToPhone(), tenantId, request.getMessage());

        // TODO: Implement Twilio integration
        // For now, just log and return pending status
        // Future implementation:
        // 1. Initialize Twilio client with account SID and auth token
        // 2. Call Twilio API to send SMS
        // 3. Update message status to SENT
        // 4. Store provider SID
        // 5. Log delivery event
        // 6. Increment rate limit counter

        log.warn("SMS sending is not yet implemented. Message saved with PENDING status.");

        return smsMessageMapper.toResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<SmsMessageResponse> listSms(UUID tenantId, MessageStatus status, Pageable pageable) {
        Page<SmsMessage> messages = status != null
                ? smsMessageRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                : smsMessageRepository.findByTenantId(tenantId, pageable);

        return messages.map(smsMessageMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SmsMessageResponse getSmsById(UUID id, UUID tenantId) {
        SmsMessage message = smsMessageRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS message not found"));

        return smsMessageMapper.toResponse(message);
    }
}
