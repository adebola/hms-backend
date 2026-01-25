package io.factorialsystems.auth.model.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateUserRequest {
    @NotBlank @Size(min = 3, max = 100) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 2, max = 100) private String firstName;
    @NotBlank @Size(min = 2, max = 100) private String lastName;
    private String phone;
    private String title;
    private String specialization;
    private String licenseNumber;
    private String department;
    private Set<String> roleCodes;
}
