package io.factorialsystems.auth.model.dto.response;
import lombok.*;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantSummaryResponse { private UUID id; private String code; private String slug; private String name; }
