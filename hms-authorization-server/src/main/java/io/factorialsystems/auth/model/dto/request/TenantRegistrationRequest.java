package io.factorialsystems.auth.model.dto.request;
import io.factorialsystems.auth.model.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantRegistrationRequest {
    @NotBlank @Size(min = 2, max = 255) private String facilityName;
    @NotNull private FacilityType facilityType;
    private FacilityLevel facilityLevel;
    private String registrationNumber;
    @NotBlank @Email private String email;
    private String phone;
    @Valid private AddressRequest address;
    @Valid @NotNull private AdminUserRequest adminUser;
    @NotNull private SubscriptionPlan subscriptionPlan;
    private String website;
    private String taxId;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddressRequest {
        private String street;
        @NotBlank private String city;
        private String lga;
        @NotBlank private String state;
        private String country;
        private String postalCode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdminUserRequest {
        @NotBlank @Size(min = 2, max = 100) private String firstName;
        @NotBlank @Size(min = 2, max = 100) private String lastName;
        @NotBlank @Email private String email;
        private String phone;
        private String title;
    }
}
