package io.factorialsystems.auth.model.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TokenResponse {
    @JsonProperty("access_token") private String accessToken;
    @JsonProperty("refresh_token") private String refreshToken;
    @JsonProperty("token_type") @Builder.Default private String tokenType = "Bearer";
    @JsonProperty("expires_in") private Long expiresIn;
    private UserResponse user;
}
