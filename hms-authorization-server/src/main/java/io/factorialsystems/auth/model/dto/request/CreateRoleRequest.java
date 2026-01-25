package io.factorialsystems.auth.model.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank @Size(min = 2, max = 50) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") private String code;
    @NotBlank @Size(min = 2, max = 100) private String name;
    @Size(max = 255) private String description;
    private Set<String> permissionCodes;
}
