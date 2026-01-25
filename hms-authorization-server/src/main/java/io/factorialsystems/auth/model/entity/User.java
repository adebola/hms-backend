package io.factorialsystems.auth.model.entity;

import io.factorialsystems.auth.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity @Table(name = "users") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tenant_id", nullable = false) private Tenant tenant;
    @Column(name = "username", nullable = false, length = 100) private String username;
    @Column(name = "email", nullable = false, length = 255) private String email;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(name = "phone", length = 20) private String phone;
    @Column(name = "profile_photo_url", length = 500) private String profilePhotoUrl;
    @Column(name = "title", length = 50) private String title;
    @Column(name = "specialization", length = 100) private String specialization;
    @Column(name = "license_number", length = 50) private String licenseNumber;
    @Column(name = "department", length = 100) private String department;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 30) private UserStatus status;
    @Column(name = "mfa_enabled", nullable = false) @Builder.Default private boolean mfaEnabled = false;
    @Column(name = "mfa_secret", length = 100) private String mfaSecret;
    @Column(name = "failed_login_attempts", nullable = false) @Builder.Default private int failedLoginAttempts = 0;
    @Column(name = "locked_until") private LocalDateTime lockedUntil;
    @Column(name = "last_login_at") private LocalDateTime lastLoginAt;
    @Column(name = "last_login_ip", length = 45) private String lastLoginIp;
    @Column(name = "password_changed_at") private LocalDateTime passwordChangedAt;
    @Column(name = "must_change_password", nullable = false) @Builder.Default private boolean mustChangePassword = false;
    @ManyToMany(fetch = FetchType.EAGER) @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default private Set<Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) @Builder.Default private Set<UserPasswordHistory> passwordHistory = new HashSet<>();
    public String getFullName() { return firstName + " " + lastName; }
    public boolean isActive() { return status == UserStatus.ACTIVE; }
    public boolean isLocked() { return status == UserStatus.LOCKED || (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())); }
    public void lock(int mins) { this.lockedUntil = LocalDateTime.now().plusMinutes(mins); this.status = UserStatus.LOCKED; }
    public void unlock() { this.lockedUntil = null; this.status = UserStatus.ACTIVE; this.failedLoginAttempts = 0; }
    public void incrementFailedLoginAttempts() { this.failedLoginAttempts++; }
    public void resetFailedLoginAttempts() { this.failedLoginAttempts = 0; }
    public void addRole(Role r) { roles.add(r); r.getUsers().add(this); }
    public Set<String> getAllPermissions() { return roles.stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).collect(Collectors.toSet()); }
}
