package io.factorialsystems.auth.model.dto.response;
import lombok.*;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionResponse { private UUID id; private String code; private String resource; private String action; private String description; }
