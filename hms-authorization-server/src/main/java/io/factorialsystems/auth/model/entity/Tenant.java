package io.factorialsystems.auth.model.entity;

import io.factorialsystems.auth.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.*;

@Entity @Table(name = "tenants") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant extends BaseEntity {
    @Column(name = "code", nullable = false, unique = true, length = 20) private String code;
    @Column(name = "slug", nullable = false, unique = true, length = 100) private String slug;
    @Column(name = "name", nullable = false, length = 255) private String name;
    @Enumerated(EnumType.STRING) @Column(name = "facility_type", nullable = false, length = 50) private FacilityType facilityType;
    @Enumerated(EnumType.STRING) @Column(name = "facility_level", length = 20) private FacilityLevel facilityLevel;
    @Column(name = "registration_number", length = 100) private String registrationNumber;
    @Column(name = "email", nullable = false, unique = true, length = 255) private String email;
    @Column(name = "phone", length = 20) private String phone;
    @Embedded private Address address;
    @Enumerated(EnumType.STRING) @Column(name = "subscription_plan", nullable = false, length = 30) private SubscriptionPlan subscriptionPlan;
    @Column(name = "subscription_start_date") private LocalDate subscriptionStartDate;
    @Column(name = "subscription_end_date") private LocalDate subscriptionEndDate;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 30) private TenantStatus status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "settings", columnDefinition = "jsonb") private Map<String, Object> settings;
    @Column(name = "logo_url", length = 500) private String logoUrl;
    @Column(name = "website", length = 255) private String website;
    @Column(name = "tax_id", length = 50) private String taxId;
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @Builder.Default private Set<User> users = new HashSet<>();
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @Builder.Default private Set<Role> customRoles = new HashSet<>();
    public boolean isActive() { return status == TenantStatus.ACTIVE; }
    public String getSchemaName() { return "tenant_" + code.toLowerCase(); }
}
