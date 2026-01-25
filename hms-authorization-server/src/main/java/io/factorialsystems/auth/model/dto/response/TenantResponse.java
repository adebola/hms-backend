package io.factorialsystems.auth.model.dto.response;
import io.factorialsystems.auth.model.enums.*;
import lombok.*;
import java.time.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantResponse {
    private UUID id;
    private String code;
    private String slug;
    private String name;
    private FacilityType facilityType;
    private FacilityLevel facilityLevel;
    private String registrationNumber;
    private String email;
    private String phone;
    private AddressResponse address;
    private SubscriptionPlan subscriptionPlan;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
    private TenantStatus status;
    private String logoUrl;
    private String website;
    private Map<String, Object> settings;
    private TenantStatistics statistics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddressResponse { private String street; private String city; private String lga; private String state; private String country; private String postalCode; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TenantStatistics { private long totalUsers; private long activeUsers; private long totalRoles; }
}
