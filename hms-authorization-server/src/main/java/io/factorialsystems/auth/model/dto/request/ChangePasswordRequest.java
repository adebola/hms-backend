package io.factorialsystems.auth.model.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required") private String currentPassword;
    @NotBlank(message = "New password is required") @Size(min = 8, max = 128) private String newPassword;
}
