package io.factorialsystems.auth.model.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Tenant code is required") private String tenantCode;
    @NotBlank(message = "Username is required") private String username;
    @NotBlank(message = "Password is required") private String password;
}
