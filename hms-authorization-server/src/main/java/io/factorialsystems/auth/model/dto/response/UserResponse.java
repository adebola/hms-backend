package io.factorialsystems.auth.model.dto.response;
import io.factorialsystems.auth.model.enums.UserStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String profilePhotoUrl;
    private String title;
    private String specialization;
    private String department;
    private UserStatus status;
    private boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private Set<String> roles;
    private Set<String> permissions;
    private TenantSummaryResponse tenant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
