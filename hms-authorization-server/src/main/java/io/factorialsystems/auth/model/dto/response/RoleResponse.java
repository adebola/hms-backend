package io.factorialsystems.auth.model.dto.response;
import lombok.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleResponse { private UUID id; private String code; private String name; private String description; private boolean systemRole; private Set<PermissionResponse> permissions; private UUID tenantId; }
