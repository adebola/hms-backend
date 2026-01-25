package io.factorialsystems.communications.service;

import io.factorialsystems.communications.model.entity.TenantSettings;
import io.factorialsystems.communications.repository.TenantSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RateLimitService {

    private final TenantSettingsService tenantSettingsService;
    private final TenantSettingsRepository settingsRepository;

    public boolean checkEmailLimit(UUID tenantId) {
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);
        resetIfNewDay(settings);
        boolean withinLimit = settings.getEmailsSentToday() < settings.getDailyEmailLimit();

        if (!withinLimit) {
            log.warn("Email rate limit exceeded for tenant: {} ({}/{})",
                    tenantId, settings.getEmailsSentToday(), settings.getDailyEmailLimit());
        }

        return withinLimit;
    }

    public boolean checkSmsLimit(UUID tenantId) {
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);
        resetIfNewDay(settings);
        boolean withinLimit = settings.getSmsSentToday() < settings.getDailySmsLimit();

        if (!withinLimit) {
            log.warn("SMS rate limit exceeded for tenant: {} ({}/{})",
                    tenantId, settings.getSmsSentToday(), settings.getDailySmsLimit());
        }

        return withinLimit;
    }

    public void incrementEmailCount(UUID tenantId) {
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);
        settings.setEmailsSentToday(settings.getEmailsSentToday() + 1);
        settingsRepository.save(settings);
        log.debug("Email count incremented for tenant: {} (now: {})",
                tenantId, settings.getEmailsSentToday());
    }

    public void incrementSmsCount(UUID tenantId) {
        TenantSettings settings = tenantSettingsService.getOrCreateSettings(tenantId);
        settings.setSmsSentToday(settings.getSmsSentToday() + 1);
        settingsRepository.save(settings);
        log.debug("SMS count incremented for tenant: {} (now: {})",
                tenantId, settings.getSmsSentToday());
    }

    private void resetIfNewDay(TenantSettings settings) {
        LocalDate today = LocalDate.now();
        LocalDate resetDate = settings.getLimitResetDate().toLocalDate();

        if (resetDate.isBefore(today)) {
            log.info("Resetting daily counters for tenant: {}", settings.getTenantId());
            settings.setEmailsSentToday(0);
            settings.setSmsSentToday(0);
            settings.setLimitResetDate(LocalDateTime.now());
            settingsRepository.save(settings);
        }
    }
}
