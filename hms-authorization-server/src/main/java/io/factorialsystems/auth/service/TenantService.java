package io.factorialsystems.auth.service;

import io.factorialsystems.auth.exception.DuplicateResourceException;
import io.factorialsystems.auth.exception.ResourceNotFoundException;
import io.factorialsystems.auth.model.dto.request.TenantRegistrationRequest;
import io.factorialsystems.auth.model.dto.response.TenantResponse;
import io.factorialsystems.auth.model.entity.*;
import io.factorialsystems.auth.model.enums.*;
import io.factorialsystems.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final AuthEventPublisher eventPublisher;
    private final CommunicationsPublisher communicationsPublisher;

    @Transactional
    public TenantResponse registerTenant(TenantRegistrationRequest request) {
        log.info("Registering new tenant: {}", request.getFacilityName());
        if (tenantRepository.existsByEmail(request.getEmail())) throw new DuplicateResourceException("Tenant", "email", request.getEmail());
        String code = generateTenantCode(request.getFacilityName());
        String slug = generateSlug(request.getFacilityName());
        Tenant tenant = Tenant.builder().code(code).slug(slug).name(request.getFacilityName()).facilityType(request.getFacilityType()).facilityLevel(request.getFacilityLevel()).registrationNumber(request.getRegistrationNumber()).email(request.getEmail()).phone(request.getPhone()).address(mapAddress(request.getAddress())).subscriptionPlan(request.getSubscriptionPlan()).subscriptionStartDate(LocalDate.now()).subscriptionEndDate(LocalDate.now().plusDays(request.getSubscriptionPlan().getTrialDays())).status(TenantStatus.PENDING_VERIFICATION).website(request.getWebsite()).taxId(request.getTaxId()).build();
        tenant = tenantRepository.save(tenant);
        String tempPassword = createAdminUser(tenant, request.getAdminUser());
        eventPublisher.publishTenantCreated(tenant);

        // Send registration confirmation email
        communicationsPublisher.sendTenantRegistrationEmail(
            tenant.getId(),
            request.getAdminUser().getEmail(),
            request.getAdminUser().getFirstName() + " " + request.getAdminUser().getLastName(),
            request.getFacilityName(),
            tenant.getCode()
        );

        // Send welcome email with temp password
        communicationsPublisher.sendWelcomeEmail(
            tenant.getId(),
            request.getAdminUser().getEmail(),
            request.getAdminUser().getFirstName() + " " + request.getAdminUser().getLastName(),
            tempPassword,
            request.getFacilityName()
        );

        log.info("Tenant registered successfully: {}", tenant.getCode());
        return mapToResponse(tenant);
    }

    @Transactional
    public TenantResponse activateTenant(UUID tenantId, String notes) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId.toString()));
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);
        eventPublisher.publishTenantActivated(tenant);
        return mapToResponse(tenant);
    }

    @Transactional
    public TenantResponse suspendTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId.toString()));
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant = tenantRepository.save(tenant);
        eventPublisher.publishTenantSuspended(tenant);
        return mapToResponse(tenant);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId.toString()));
        return mapToResponse(tenant);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantByCode(String code) {
        Tenant tenant = tenantRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Tenant", code));
        return mapToResponse(tenant);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> listTenants(TenantStatus status, String search, Pageable pageable) {
        return tenantRepository.searchTenants(status, search, pageable).map(this::mapToResponse);
    }

    private String createAdminUser(Tenant tenant, TenantRegistrationRequest.AdminUserRequest adminRequest) {
        Role tenantAdminRole = roleRepository.findSystemRoleByCode("TENANT_ADMIN").orElseThrow(() -> new ResourceNotFoundException("Role", "TENANT_ADMIN"));
        String tempPassword = passwordService.generateTemporaryPassword();
        User adminUser = User.builder().tenant(tenant).username(adminRequest.getEmail().split("@")[0]).email(adminRequest.getEmail()).passwordHash(passwordEncoder.encode(tempPassword)).firstName(adminRequest.getFirstName()).lastName(adminRequest.getLastName()).phone(adminRequest.getPhone()).title(adminRequest.getTitle()).status(UserStatus.PENDING_VERIFICATION).mustChangePassword(true).build();
        adminUser.addRole(tenantAdminRole);
        userRepository.save(adminUser);
        log.info("Admin user created for tenant {}: {}", tenant.getCode(), adminUser.getEmail());
        return tempPassword;
    }

    private String generateTenantCode(String name) {
        String base = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (base.length() > 3) base = base.substring(0, 3); else base = String.format("%-3s", base).replace(' ', 'X');
        String code = base; int counter = 1;
        while (tenantRepository.existsByCode(code)) code = base + counter++;
        return code;
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        String baseSlug = slug; int counter = 1;
        while (tenantRepository.existsBySlug(slug)) slug = baseSlug + "-" + counter++;
        return slug;
    }

    private Address mapAddress(TenantRegistrationRequest.AddressRequest request) {
        if (request == null) return null;
        return Address.builder().street(request.getStreet()).city(request.getCity()).lga(request.getLga()).state(request.getState()).country(request.getCountry()).postalCode(request.getPostalCode()).build();
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .code(tenant.getCode())
                .slug(tenant.getSlug())
                .name(tenant.getName())
                .facilityType(tenant.getFacilityType())
                .facilityLevel(tenant.getFacilityLevel())
                .registrationNumber(tenant.getRegistrationNumber())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .address(
                        tenant.getAddress() != null ?
                                TenantResponse.AddressResponse.builder()
                                        .street(tenant.getAddress().getStreet())
                                        .city(tenant.getAddress().getCity())
                                        .lga(tenant.getAddress().getLga())
                                        .state(tenant.getAddress().getState())
                                        .country(tenant.getAddress().getCountry())
                                        .postalCode(tenant.getAddress().getPostalCode())
                                        .build() : null
                )
                .subscriptionPlan(tenant.getSubscriptionPlan())
                .subscriptionStartDate(tenant.getSubscriptionStartDate())
                .subscriptionEndDate(tenant.getSubscriptionEndDate())
                .status(tenant.getStatus())
                .logoUrl(tenant.getLogoUrl())
                .website(tenant.getWebsite())
                .settings(tenant.getSettings())
                .statistics(
                        TenantResponse.TenantStatistics.builder()
                                .totalUsers(userRepository.countByTenantId(tenant.getId()))
                                .activeUsers(userRepository.countByTenantIdAndStatus(tenant.getId(), UserStatus.ACTIVE))
                                .totalRoles(roleRepository.findByTenantId(tenant.getId()).size())
                                .build()
                )
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
