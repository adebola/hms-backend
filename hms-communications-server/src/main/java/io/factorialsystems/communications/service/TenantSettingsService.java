package io.factorialsystems.communications.service;

import io.factorialsystems.communications.config.CommunicationsProperties;
import io.factorialsystems.communications.model.entity.TenantSettings;
import io.factorialsystems.communications.repository.TenantSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantSettingsService {

    private final TenantSettingsRepository settingsRepository;
    private final CommunicationsProperties properties;

    @Cacheable(value = "tenantSettings", key = "#tenantId")
    public TenantSettings getOrCreateSettings(UUID tenantId) {
        return settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));
    }

    private TenantSettings createDefaultSettings(UUID tenantId) {
        log.info("Creating default settings for tenant: {}", tenantId);

        TenantSettings settings = TenantSettings.builder()
                .tenantId(tenantId)
                .defaultFromEmail(properties.getBrevo().getDefaultFromEmail())
                .defaultFromName(properties.getBrevo().getDefaultFromName())
                .defaultFromPhone(properties.getSms().getDefaultFromPhone())
                .dailyEmailLimit(properties.getRateLimit().getDefaultDailyEmailLimit())
                .dailySmsLimit(properties.getRateLimit().getDefaultDailySmsLimit())
                .emailsSentToday(0)
                .smsSentToday(0)
                .limitResetDate(LocalDateTime.now())
                .enableOpenTracking(true)
                .enableClickTracking(true)
                .build();

        return settingsRepository.save(settings);
    }

    public TenantSettings updateSettings(UUID tenantId, TenantSettings updatedSettings) {
        TenantSettings settings = getOrCreateSettings(tenantId);

        // Update only the settings fields, not the counters
        if (updatedSettings.getDefaultFromEmail() != null) {
            settings.setDefaultFromEmail(updatedSettings.getDefaultFromEmail());
        }
        if (updatedSettings.getDefaultFromName() != null) {
            settings.setDefaultFromName(updatedSettings.getDefaultFromName());
        }
        if (updatedSettings.getEmailSignature() != null) {
            settings.setEmailSignature(updatedSettings.getEmailSignature());
        }
        if (updatedSettings.getDefaultFromPhone() != null) {
            settings.setDefaultFromPhone(updatedSettings.getDefaultFromPhone());
        }
        if (updatedSettings.getDailyEmailLimit() != null) {
            settings.setDailyEmailLimit(updatedSettings.getDailyEmailLimit());
        }
        if (updatedSettings.getDailySmsLimit() != null) {
            settings.setDailySmsLimit(updatedSettings.getDailySmsLimit());
        }
        if (updatedSettings.getEnableOpenTracking() != null) {
            settings.setEnableOpenTracking(updatedSettings.getEnableOpenTracking());
        }
        if (updatedSettings.getEnableClickTracking() != null) {
            settings.setEnableClickTracking(updatedSettings.getEnableClickTracking());
        }

        return settingsRepository.save(settings);
    }
}
