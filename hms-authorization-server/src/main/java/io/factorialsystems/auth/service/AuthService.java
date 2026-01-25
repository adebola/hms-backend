package io.factorialsystems.auth.service;

import io.factorialsystems.auth.config.AuthProperties;
import io.factorialsystems.auth.exception.AuthenticationException;
import io.factorialsystems.auth.exception.ResourceNotFoundException;
import io.factorialsystems.auth.model.dto.request.ChangePasswordRequest;
import io.factorialsystems.auth.model.dto.request.LoginRequest;
import io.factorialsystems.auth.model.dto.response.*;
import io.factorialsystems.auth.model.entity.*;
import io.factorialsystems.auth.model.enums.*;
import io.factorialsystems.auth.repository.*;
import io.factorialsystems.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordService passwordService;
    private final AuditService auditService;
    private final AuthProperties authProperties;

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for user: {} with tenant code: {}", request.getUsername(), request.getTenantCode());
        Tenant tenant = resolveTenant(request.getTenantCode());
        if (!tenant.isActive()) {
            auditService.logAuthEvent(tenant.getId(), null, request.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "Tenant not active");
            throw AuthenticationException.tenantInactive();
        }
        User user = userRepository.findByTenantIdAndUsernameOrEmail(tenant.getId(), request.getUsername()).orElseThrow(() -> {
            auditService.logAuthEvent(tenant.getId(), null, request.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "User not found");
            return AuthenticationException.invalidCredentials();
        });
        if (user.isLocked()) {
            auditService.logAuthEvent(tenant.getId(), user.getId(), user.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "Account locked");
            throw AuthenticationException.accountLocked();
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress, userAgent);
            throw AuthenticationException.invalidCredentials();
        }
        if (!user.isActive()) {
            auditService.logAuthEvent(tenant.getId(), user.getId(), user.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "Account not active");
            throw AuthenticationException.accountInactive();
        }
        if (isPasswordExpired(user)) {
            auditService.logAuthEvent(tenant.getId(), user.getId(), user.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "Password expired");
            throw AuthenticationException.passwordExpired();
        }
        userRepository.updateLoginSuccess(user.getId(), LocalDateTime.now(), ipAddress);
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        auditService.logAuthEvent(tenant.getId(), user.getId(), user.getUsername(), AuthEventType.LOGIN_SUCCESS, ipAddress, userAgent, true, null);
        log.info("Login successful for user: {}", user.getUsername());
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").expiresIn(authProperties.getJwt().getAccessTokenValidityMinutes() * 60L).user(mapToUserResponse(user)).build();
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) throw AuthenticationException.invalidToken();
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(AuthenticationException::invalidToken);
        if (!user.isActive()) throw AuthenticationException.accountInactive();
        if (!user.getTenant().isActive()) throw AuthenticationException.tenantInactive();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        return TokenResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).tokenType("Bearer").expiresIn(authProperties.getJwt().getAccessTokenValidityMinutes() * 60L).build();
    }

    @Transactional
    public void logout(String userId, String refreshToken) {
        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
        if (user != null) auditService.logAuthEvent(user.getTenant().getId(), user.getId(), user.getUsername(), AuthEventType.LOGOUT, null, null, true, null);
        if (refreshToken != null) jwtTokenProvider.invalidateToken(refreshToken);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) throw AuthenticationException.invalidCredentials();
        passwordService.validatePassword(request.getNewPassword());
        passwordService.validatePasswordHistory(user, request.getNewPassword());
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        passwordService.savePasswordHistory(user, user.getPasswordHash());
        user.setPasswordHash(newPasswordHash);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
        auditService.logAuthEvent(user.getTenant().getId(), user.getId(), user.getUsername(), AuthEventType.PASSWORD_CHANGE, null, null, true, null);
        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToUserResponse(user);
    }

    private Tenant resolveTenant(String tenantCode) {
        if (tenantCode == null || tenantCode.isEmpty()) throw new AuthenticationException("TENANT_REQUIRED", "Tenant code is required");
        return tenantRepository.findByCode(tenantCode).orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantCode));
    }

    private void handleFailedLogin(User user, String ipAddress, String userAgent) {
        user.incrementFailedLoginAttempts();
        var lockoutConfig = authProperties.getLockout();
        if (user.getFailedLoginAttempts() >= lockoutConfig.getMaxFailedAttempts()) {
            user.lock(lockoutConfig.getLockoutDurationMinutes());
            user.setStatus(UserStatus.LOCKED);
            auditService.logAuthEvent(user.getTenant().getId(), user.getId(), user.getUsername(), AuthEventType.ACCOUNT_LOCKED, ipAddress, userAgent, false, "Locked after " + lockoutConfig.getMaxFailedAttempts() + " failed attempts");
        }
        userRepository.save(user);
        auditService.logAuthEvent(user.getTenant().getId(), user.getId(), user.getUsername(), AuthEventType.LOGIN_FAILURE, ipAddress, userAgent, false, "Invalid password");
    }

    private boolean isPasswordExpired(User user) {
        if (user.isMustChangePassword()) return true;
        int maxAgeDays = authProperties.getPassword().getMaxAgeDays();
        if (maxAgeDays <= 0 || user.getPasswordChangedAt() == null) return false;
        return user.getPasswordChangedAt().plusDays(maxAgeDays).isBefore(LocalDateTime.now());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName()).fullName(user.getFullName()).phone(user.getPhone()).profilePhotoUrl(user.getProfilePhotoUrl()).title(user.getTitle()).specialization(user.getSpecialization()).department(user.getDepartment()).status(user.getStatus()).mfaEnabled(user.isMfaEnabled()).lastLoginAt(user.getLastLoginAt()).roles(user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet())).permissions(user.getAllPermissions()).tenant(TenantSummaryResponse.builder().id(user.getTenant().getId()).code(user.getTenant().getCode()).slug(user.getTenant().getSlug()).name(user.getTenant().getName()).build()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
}
